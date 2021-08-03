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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
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
            if(!line.contains("error-prone")) {
                continue;
            }
            if(!line.equals(projectUrl)) {
                if(proj != null) {
                    if(!proj.isDone()) {
                        proj.setDone();
                        proj.saveIt();
                    }
                }
                projectUrl = line;
                projectName = projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
                proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
                if (proj == null) {
                    System.out.println("Starting " + projectName);
                    proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectUrl, projectName);
                    proj.saveIt();
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
            if(scenarioName.contains("statistics")) {
                continue;
            }
            replicateMergeScenario(scenario.getName(), path + "/" + scenarioName, project, path + "/" + "statistics.csv");
        }
    }

    /*
     * Run both versions of IntelliMerge on the given merge scenario.
     */
    private static void replicateMergeScenario(String mergeCommitHash, String path, Project project, String csvPath)
            throws Exception {

        File[] directories = new File(path).listFiles();
        File base = directories[0];
        File theirs = directories[1];
        File ours = directories[2];
        File gitMerged = directories[3];
        File manuallyMerged = null;
        if(directories.length > 4) {
            manuallyMerged = directories[4];
        }


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
        else if(mergeCommit.isDone()) {
            return;
        }
        else if(!mergeCommit.isDone()) {
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
        String gitMergedPath = gitMerged.getAbsolutePath();
        // Run IntelliMerge with getDirectories()
        String resultPath = "intelliMergeResults";
        String intelliMergeResultPath = path + "/" + resultPath;
        long time = runIntelliMerge(ours, base, theirs, intelliMergeResultPath, path);

        String gitTemp = gitMerged + "temp";
        String intelliMergeTemp = intelliMergeResultPath + "temp";
        copyDir(intelliMergeResultPath, intelliMergeTemp);
        copyDir(gitMergedPath, gitTemp);
        //getManualFiles(modifiedResultsPath, path, manualResults);

        String manuallyMergedPath = "";
        String formattedPath = manuallyMergedPath + "Formatted";
        if(manuallyMerged != null) {
            manuallyMergedPath = manuallyMerged.getAbsolutePath();
            formattedPath = manuallyMergedPath + "Formatted";
            copyDir(manuallyMergedPath, formattedPath);
            formatAllJavaFiles(formattedPath);


        }

        EvaluationUtils.removeAllComments(intelliMergeTemp);
        EvaluationUtils.removeAllComments(gitTemp);
        EvaluationUtils.removeAllComments(manuallyMergedPath);
        EvaluationUtils.removeAllComments(formattedPath);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> intelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(intelliMergeTemp);

        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> gitMergeConflicts =
                EvaluationUtils.extractMergeConflicts(gitTemp);

        // Get manually merged java files
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMerged);

        ArrayList<SourceFile> formattedMergedFiles = EvaluationUtils
                .getJavaSourceFiles(formattedPath, new ArrayList<>(), new File(formattedPath));

        // Compare tools with manually merged code
        ComparisonResult intelliVsManual = EvaluationUtils.compareAutoMerged(intelliMergeTemp, formattedMergedFiles, path, true);
        ComparisonResult gitVsManual = EvaluationUtils.compareAutoMerged(gitTemp, manuallyMergedFiles, path, true);


        // Add IntelliMerge data to database
        MergeResult intelliMergeResult = new MergeResult("IntelliMerge", intelliMergeConflicts.getLeft().getLeft(),
                intelliMergeConflicts.getLeft().getRight(), time, intelliVsManual, mergeCommit);
        intelliMergeResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflictingFiles = intelliMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(intelliMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }

        // Add IntelliMerge data to database
        MergeResult gitMergeResult = new MergeResult("gitMerge", gitMergeConflicts.getLeft().getLeft(),
                gitMergeConflicts.getLeft().getRight(), time, gitVsManual, mergeCommit);
        gitMergeResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> gitMergeConflictingFiles = gitMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(intelliMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
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
    private static long runIntelliMerge(File ours, File base, File theirs, String output, String path) throws Exception {
        System.out.println("Starting IntelliMerge");
        List<String> directories = new ArrayList<>();
        directories.add(ours.getAbsolutePath());
        directories.add(base.getAbsolutePath());
        directories.add(theirs.getAbsolutePath());
        try {
            // None of the values are used, so setting to null for now
            APIClient apiClient = new APIClient(null, null, null, null, null, false);
            long time = System.currentTimeMillis();
            apiClient.processDirectory(path, output);
            // merge.mergeDirectories(directories, output);
            long time2 = System.currentTimeMillis();
            return time2 - time;
        }
        catch(IndexOutOfBoundsException e) {
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
     * Clone the given project.
     */
    private static void cloneProject(String path, String url) {
        String projectName = url.substring(url.lastIndexOf("/"));
        String clonePath = path + projectName;
        try {
            Git.cloneRepository().setURI(url).setDirectory(new File(clonePath)).call();
        }
        catch(GitAPIException | JGitInternalException e) {
            e.printStackTrace();
        }
    }

    private static void getManualFiles(String result, String projectPath, String manualPath) {

        File resultDir = new File(result);
        ArrayList<SourceFile> resultFiles = EvaluationUtils.getJavaSourceFiles(result, new ArrayList<>(), resultDir);
        for(SourceFile resultFile : resultFiles) {
            String relativePath = resultFile.getRelativePath();
            String projectFilePath = projectPath + "/" + relativePath;
            File manualFile = new File(projectFilePath);
            if(manualFile.exists()) {
                String manualFilePath = manualPath + "/" + relativePath;
                try {
                    File targetDirectory = new File(manualFilePath.substring(0, manualFilePath.lastIndexOf("/")));
                    if(!targetDirectory.exists()) {
                        targetDirectory.mkdirs();
                    }

                    File targetFile = new File(manualFilePath);
                    Files.copy(manualFile.toPath(), targetFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
