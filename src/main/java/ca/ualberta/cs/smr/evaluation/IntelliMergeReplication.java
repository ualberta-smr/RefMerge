package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.data.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.data.ConflictBlockData;
import ca.ualberta.cs.smr.evaluation.data.ConflictingFileData;
import ca.ualberta.cs.smr.evaluation.data.SourceFile;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IntelliMergeReplication {
    private final static String UNMODIFIED = "IntelliMerge-1.0.7-all.jar";
    private final static String MODIFIED = "IntelliMerge-1.0.7-modified.jar";


    /*
     * Use the IntelliMerge dataset to try to replicate the IntelliMerge results with both versions of IntelliMerge.
     */
    public static void runIntelliMergeReplication(String path) throws IOException, GitAPIException {
        Utils.clearTemp("projects");
        URL url = EvaluationPipeline.class.getResource("/intelliMerge_data");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            String[] values = line.split(";");
            if(!values[0].contains("error-prone")) {
                continue;
            }
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
    private static void replicateMergeScenario(String mergeCommitHash, String path, Project project) throws IOException, GitAPIException {
        Utils.clearTemp("unmodifiedResults");
        Utils.clearTemp("modifiedResults");
        Utils.clearTemp("intelliMerge");
        Utils.clearTemp("manualMerge");
        File file = new File(path);
        Git git = Git.open(file);
        GitUtils.gitReset(git);

        String manualResults = System.getProperty("user.home") + "/temp/manualMerge";
        File manualDir = new File(manualResults);
        manualDir.mkdirs();
        GitUtils.checkoutForReplication(git, mergeCommitHash);
        Utils.runSystemCommand("cp", "-r", path + "/.", manualResults);
        Repository repository = git.getRepository();
        ObjectId id = repository.resolve(mergeCommitHash);
        RevCommit mergeCommitRev = git.getRepository().parseCommit(id);
        RevCommit leftParent = mergeCommitRev.getParent(0);
        RevCommit rightParent = mergeCommitRev.getParent(1);
        String baseCommit = null;
        try {
            baseCommit = GitUtils.getBaseCommit(leftParent, rightParent, git.getRepository());
        } catch (IOException e) {
            e.printStackTrace();
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


        String leftPath = System.getProperty("user.home") + "/temp/intelliMerge/ours";
        String rightPath = System.getProperty("user.home") + "/temp/intelliMerge/theirs";
        String basePath = System.getProperty("user.home") + "/temp/intelliMerge/base";
        File leftFile = new File(leftPath);
        File baseFile = new File(basePath);
        File rightFile = new File(rightPath);
        leftFile.mkdirs();
        baseFile.mkdirs();
        rightFile.mkdirs();
        GitUtils.checkoutForReplication(git, leftParent.getName());
        Utils.runSystemCommand("cp", "-r", path + "/.", leftPath);
        GitUtils.checkoutForReplication(git, rightParent.getName());
        Utils.runSystemCommand("cp", "-r", path + "/.", rightPath);
        GitUtils.checkoutForReplication(git, baseCommit);
        Utils.runSystemCommand("cp", "-r", path + "/.", basePath);

        String unmodifiedResultsPath = System.getProperty("user.home") + "/temp/unmodifiedResults";
        String modifiedResultsPath = System.getProperty("user.home") + "/temp/modifiedResults";
        // Run unmodified IntelliMerge
        long unmodifiedTime = runIntelliMerge(leftPath, basePath, rightPath, UNMODIFIED, "unmodifiedResults");
        // Run modified IntelliMerge
        long modifiedTime = runIntelliMerge(leftPath, basePath, rightPath, MODIFIED, "modifiedResults");

        // Remove all comments from all directories
        EvaluationUtils.removeAllComments(unmodifiedResultsPath);
        EvaluationUtils.removeAllComments(modifiedResultsPath);
        EvaluationUtils.removeAllComments(manualResults);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> unmodifiedIntelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(unmodifiedResultsPath);
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> modifiedIntelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(modifiedResultsPath);

        // Get manually merged java files
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manualResults, new ArrayList<>(), manualDir);

        // Compare tools with manually merged code
        ComparisonResult unmodifiedVsManual = EvaluationUtils.compareAutoMerged(unmodifiedResultsPath, manuallyMergedFiles, path, true);
        ComparisonResult modifiedVsManual = EvaluationUtils.compareAutoMerged(modifiedResultsPath, manuallyMergedFiles, path, true);

        // Add IntelliMerge data to database
        MergeResult unmodifiedResult = new MergeResult("IntelliMerge_unmodified", unmodifiedIntelliMergeConflicts.getLeft().getLeft(),
                unmodifiedIntelliMergeConflicts.getLeft().getRight(), unmodifiedTime, unmodifiedVsManual, mergeCommit);
        unmodifiedResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> unmodifiedConflictingFiles = unmodifiedIntelliMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : unmodifiedConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(unmodifiedResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }

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
    private static long runIntelliMerge(String leftPath, String basePath, String rightPath, String intelliMergeVersion, String output) {
        String outputDir = System.getProperty("user.home") + "/temp/" + output;
        String jarFile =  System.getProperty("user.home") + "/temp/" + intelliMergeVersion;
        System.out.println("Starting IntelliMerge");
        long time = System.currentTimeMillis();
        try {
            Utils.runSystemCommand("java", "-jar", jarFile, "-d", leftPath, basePath, rightPath, "-o", outputDir);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("IntelliMerge is done");
        return time2 - time;
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

}
