package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.data.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.data.ConflictBlockData;
import ca.ualberta.cs.smr.evaluation.data.ConflictingFileData;
import ca.ualberta.cs.smr.evaluation.data.SourceFile;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import edu.pku.intellimerge.client.IntelliMerge;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

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
        URL url = EvaluationPipeline.class.getResource("/intelliMerge_data");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            String[] values = line.split(";");
            if(!values[0].equals(projectUrl)) {
                if(proj != null) {
                    if(!proj.isDone()) {
                        proj.setDone();
                        proj.saveIt();
                    }
                }
                projectUrl = values[0];
                projectName = projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
                proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
                if (proj == null) {
                    System.out.println("Starting " + projectName);
                    proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectUrl, projectName);
                    proj.saveIt();
                    cloneProject(path, projectUrl);
                }
            }
            replicateMergeScenario(values[1], path + "/" + projectName, proj);
        }
        if(proj != null) {
            if(!proj.isDone()) {
                proj.setDone();
                proj.saveIt();
            }
        }
        Utils.clearTemp("projects");
    }

    /*
     * Run both versions of IntelliMerge on the given merge scenario.
     */
    private static void replicateMergeScenario(String mergeCommitHash, String path, Project project) throws Exception {
        Utils.clearTemp("unmodifiedResults");
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("intelliMerge");
        Utils.clearTemp("manualMerge");
        File file = new File(path);
        Git git = Git.open(file);
        GitUtils.gitReset(git);

        String manualResults = System.getProperty("user.home") + "/temp/manualMerge";
        File manualDir = new File(manualResults);
        manualDir.mkdirs();
        try {
            GitUtils.checkoutForReplication(git, mergeCommitHash);
        }
        catch(Exception e) {
            System.out.println("Skipping: " + mergeCommitHash);
            return;
        }
        Repository repository = git.getRepository();
        ObjectId id = repository.resolve(mergeCommitHash);
        RevCommit mergeCommitRev = git.getRepository().parseCommit(id);
        RevCommit leftParent = mergeCommitRev.getParent(0);
        RevCommit rightParent = mergeCommitRev.getParent(1);
        String baseCommit;
        try {
            baseCommit = GitUtils.getBaseCommit(leftParent, rightParent, git.getRepository());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MergeCommit mergeCommit = MergeCommit.findFirst("commit_hash = ?", mergeCommitHash);
        if(mergeCommit == null) {
            mergeCommit = new MergeCommit(mergeCommitHash, true, leftParent.getName(),
                    rightParent.getName(), project, mergeCommitRev.getCommitTime());
            mergeCommit.saveIt();
        }
        else if(mergeCommit.isDone()) {
            return;
        }
        else if(!mergeCommit.isDone()) {
            mergeCommit.delete();
            mergeCommit = new MergeCommit(mergeCommitHash, true, leftParent.getName(),
                    rightParent.getName(), project, mergeCommitRev.getCommitTime());
            mergeCommit.saveIt();
        }

        List<String> commits = new ArrayList<>();
        commits.add(rightParent.getName());
        commits.add(baseCommit);
        commits.add(leftParent.getName());

        String modifiedResultsPath = System.getProperty("user.home") + "/temp/intelliMergeResults";
        // Run modified IntelliMerge
        String resultPath = "intelliMergeResults";
        long modifiedTime = runIntelliMerge(path, commits, resultPath);

        GitUtils.checkoutForReplication(git, mergeCommitHash);
        getManualFiles(modifiedResultsPath, path, manualResults);

        EvaluationUtils.removeAllComments(modifiedResultsPath);
        EvaluationUtils.removeAllComments(manualResults);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> modifiedIntelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(modifiedResultsPath);

        // Get manually merged java files
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manualResults, new ArrayList<>(), manualDir);

        // Compare tools with manually merged code
        ComparisonResult modifiedVsManual = EvaluationUtils.compareAutoMerged(modifiedResultsPath, manuallyMergedFiles, path, true);



        // Add IntelliMerge data to database
        MergeResult modifiedMergeResult = new MergeResult("IntelliMerge_modified", modifiedIntelliMergeConflicts.getLeft().getLeft(),
                modifiedIntelliMergeConflicts.getLeft().getRight(), modifiedTime, modifiedVsManual, mergeCommit);
        modifiedMergeResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> modifiedConflictingFiles = modifiedIntelliMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : modifiedConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(modifiedMergeResult, pair.getLeft());
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
    private static long runIntelliMerge(String repoPath, List<String> commits, String output) throws Exception{
        String outputDir = System.getProperty("user.home") + "/temp/" + output;
        System.out.println("Starting IntelliMerge");
        long time = System.currentTimeMillis();
        try {
            IntelliMerge merge = new IntelliMerge();
            // our commit, base commit, their commit in that order
            List<Long> times = merge.mergeBranchesForRefMergeEvaluation(repoPath, commits, outputDir, true);
            long time2 = System.currentTimeMillis();
            System.out.println("Collection took: " + times.get(0));
            System.out.println("Building took: " + times.get(1));
            System.out.println("Matching took: " + times.get(2));
            System.out.println("Merging took: " + times.get(3));
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

}
