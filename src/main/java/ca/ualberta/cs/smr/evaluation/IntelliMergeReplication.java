package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.data.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.data.ConflictBlockData;
import ca.ualberta.cs.smr.evaluation.data.ConflictingFileData;
import ca.ualberta.cs.smr.evaluation.data.SourceFile;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import edu.pku.intellimerge.client.APIClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.errors.LargeObjectException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IntelliMergeReplication {


    /*
     * Use the IntelliMerge dataset to try to replicate the IntelliMerge results with both versions of IntelliMerge.
     */
    public static void runIntelliMergeReplication(String path) throws Exception {
        URL url = EvaluationPipeline.class.getResource("/intelliMerge_replication_projects");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
//            if(line.contains("junit") || line.contains("gradle") || line.contains("error-prone") ||
//                line.contains("storm") || line.contains("javaparser") || line.contains("antlr") || line.contains("deeplearning")) {
//                continue;
//            }
            if(!line.equals(projectUrl)) {
                projectUrl = line;
                projectName = projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
                proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
                if (proj == null) {
                    System.out.println("Starting " + projectName);
                    proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectUrl, projectName);
                    proj.saveIt();
                }
                if(proj.isDone()) {
                    continue;
                }
            }
            replicateProject(path + "/" + projectName, proj);
        }
        if(proj != null) {
            if(!proj.isDone()) {
                proj.setDone();
                proj.saveIt();
            }
        }
    }

    /*
     * Use the project path to get data for each merge scenario and replicate.
     */
    private static void replicateProject(String path, Project project) throws Exception {
        File projectDir = new File(path);
        File[] scenarios = projectDir.listFiles();
        for(File scenario : scenarios) {
            String scenarioName = scenario.getName();
            if(scenarioName.contains("statistics") || scenarioName.contains("involved_refactorings")) {
                continue;
            }
            replicateMergeScenario(scenario.getName(), path + "/" + scenarioName, project, path + "/" + "statistics.csv");
        }
    }

    /*
     * Run both versions of IntelliMerge on the given merge scenario.
     */
    private static void replicateMergeScenario(String mergeCommitHash, String path, Project project, String csvPath) throws Exception {

        File[] directories = new File(path).listFiles();
        File gitMerged = directories[3];
        File manuallyMerged;
        if(directories.length < 5) {
            return;
        }
        manuallyMerged = directories[4];

        File csvFile = new File(csvPath);

        MergeCommit mergeCommit = MergeCommit.findFirst("commit_hash = ?", mergeCommitHash);
        if(mergeCommit == null) {
            List<String> lines = getLinesFromInputStream(csvFile.toURI().toURL().openStream());
            for(String line : lines) {
                String[] values = line.split(";");
                if(values[0].equals(mergeCommitHash)) {
                    mergeCommit = new MergeCommit(mergeCommitHash, true, values[1],
                            values[2], project, 0);
                    mergeCommit.saveIt();
                    break;
                }
            }
        }
        else  {
            return;
        }
        if(mergeCommit == null) {
            return;
        }
        String gitMergedPath = gitMerged.getAbsolutePath();
        // Run IntelliMerge with getDirectories()
        String resultPath = "intelliMergeResults";
        String intelliMergeResultPath = path + "/" + resultPath;
        long time = runIntelliMerge(intelliMergeResultPath, path);

        String gitTemp = gitMerged + "temp";
        String intelliMergeTemp = intelliMergeResultPath + "temp";
        copyDir(intelliMergeResultPath, intelliMergeTemp);
        copyDir(gitMergedPath, gitTemp);
        //getManualFiles(modifiedResultsPath, path, manualResults);
        String manuallyMergedPath = manuallyMerged.getAbsolutePath();
        String formattedPath = manuallyMergedPath + "Formatted";
        copyDir(manuallyMergedPath, formattedPath);

//        List<String> refactoringConflicts = EvaluationUtils.readFileToLines(refactoringConflictsFile.getAbsolutePath());
//        List<String> relativePaths = EvaluationUtils.getRelativePathsFromRefactoringConflicts(mergeCommitHash, refactoringConflicts);
        List<String> relativePaths = new ArrayList<>();

        EvaluationUtils.removeAllComments(intelliMergeTemp);
        EvaluationUtils.removeAllComments(gitTemp);
        EvaluationUtils.removeAllComments(manuallyMergedPath);
        EvaluationUtils.removeAllComments(formattedPath);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(intelliMergeTemp, "IntelliMerge", true);

        List<Pair<ConflictingFileData, List<ConflictBlockData>>> gitMergeConflicts =
                EvaluationUtils.extractMergeConflicts(gitTemp, "GitMerge", false);

        formatAllJavaFiles(formattedPath);

        for(Pair<ConflictingFileData, List<ConflictBlockData>> conflictPair : gitMergeConflicts) {
                if (!relativePaths.contains(conflictPair.getLeft().getFilePath())) {
                    relativePaths.add(conflictPair.getLeft().getFilePath());
                }
        }

        // Get manually merged java files
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMerged);

        ArrayList<SourceFile> formattedMergedFiles = EvaluationUtils
                .getJavaSourceFiles(formattedPath, new ArrayList<>(), new File(formattedPath));

        // Compare tools with manually merged code
        ComparisonResult intelliVsManual = EvaluationUtils
                .compareAutoMerged(intelliMergeTemp, formattedMergedFiles, path, relativePaths, true);
        ComparisonResult gitVsManual = EvaluationUtils
                .compareAutoMerged(gitTemp, manuallyMergedFiles, path, relativePaths, true);


        // Add IntelliMerge data to database
        int totalConflictingLOC = 0;
        int totalConflicts = 0;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
        }
        MergeResult intelliMergeResult = new MergeResult("IntelliMerge", totalConflicts,
                totalConflictingLOC, time, intelliVsManual, mergeCommit);
        intelliMergeResult.saveIt();
        // Add conflicting files to database
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
            ConflictingFile conflictingFile = new ConflictingFile(intelliMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData, true);
                conflictBlock.saveIt();
            }
        }

        // Add GitMerge data to database
        totalConflictingLOC = 0;
        totalConflicts = 0;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
        }
        MergeResult gitMergeResult = new MergeResult("GitMerge", totalConflicts, totalConflictingLOC,
                 time, gitVsManual, mergeCommit);
        gitMergeResult.saveIt();
        // Add conflicting files to database

        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflicts) {
            ConflictingFile conflictingFile = new ConflictingFile(gitMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData, true);
                conflictBlock.saveIt();
            }
        }



        mergeCommit.setDone();
        mergeCommit.saveIt();


    }

    /*
     * Merge the left and right parent using the specified IntelliMerge via command line. Return how long it takes for IntelliMerge
     * to finish.
     */
    private static long runIntelliMerge(String output, String path) throws Exception {
        System.out.println("Starting IntelliMerge");
        try {
            // None of the values are used, so setting to null for now except for hasMultiple
            APIClient apiClient = new APIClient(null, null, null, null, null, true);
            long time = System.currentTimeMillis();
            apiClient.processDirectory(path, output);
            long time2 = System.currentTimeMillis();
            System.out.println("IntelliMerge is done");
            return time2 - time;
        }
        catch(IndexOutOfBoundsException | OutOfMemoryError | LargeObjectException.OutOfMemory e) {
            e.printStackTrace();

        }
        System.out.println("IntelliMerge is done");
        return -1;
    }

    /*
     * Get each line from the input stream containing the IntelliMerge dataset.
     */
    private static ArrayList<String> getLinesFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }

    /*
     * Format all java files with google-java-formatter in a directory
     */
    public static void formatAllJavaFiles(String dir) {
        // read file content as string
        File file = new File(dir);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    formatAllJavaFiles(f.getAbsolutePath());
                } else if (f.isFile() && isJavaFile(f)) {
                    String code = EvaluationUtils.readFileToString(f);
                    try {
                        // format with google-java-formatter
                        String reformattedCode = new Formatter().formatSource(code);
                        // write string back into the original file
                        EvaluationUtils.writeContent(f.getAbsolutePath(), reformattedCode);
                    } catch (FormatterException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static boolean isJavaFile(File file) {
        return file.getName().toLowerCase().contains(".java");
    }



    public static void copyDir(String sourceDir, String targetDir) {
        File srcDir = new File(sourceDir);
        if (srcDir.exists()) {
            File destDir = new File(targetDir);
            try {
                FileUtils.copyDirectory(srcDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
