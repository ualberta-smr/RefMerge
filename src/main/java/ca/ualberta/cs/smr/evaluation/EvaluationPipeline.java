package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.javalite.activejdbc.Base;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import ca.ualberta.cs.smr.core.RefMerge;

import java.io.File;
import java.util.*;

public class EvaluationPipeline implements ApplicationStarter {
    private Project project;

    @Override
    public String getCommandName() {
        return "evaluation";
    }

    @Override
    public void main(@NotNull List<String> args) {
        try {
            DatabaseUtils.createDatabase();
            String path = System.getProperty("user.home") + args.get(1);
            File pathToProject = new File(path);
            this.project = ProjectUtil.openOrImport(pathToProject.toPath(), null, false);
            GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
            List<GitRepository> repos = repoManager.getRepositories();
            GitRepository repo = repos.get(0);
            System.out.println(repo);
            startEvaluation(repo);
        } catch(Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private void startEvaluation(GitRepository repo) {
        try {
            Base.open();
            runEvaluation(repo);
            Base.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    private void runEvaluation(GitRepository repo) {
        // Add project to database
        String projectURL = repo.getPresentableUrl();
        String projectName = projectURL.substring(projectURL.lastIndexOf('/') + 1);
        ca.ualberta.cs.smr.evaluation.database.Project proj =
                ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectURL);
        if (proj == null) {
            proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectURL, projectName);
            proj.saveIt();
        }

            Utils.clearTemp("manualMerge");
        Utils.clearTemp("refMergeResults");
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("gitMergeResults");
        String clonedDest = this.project.getBasePath();
        assert clonedDest != null;
        GitUtils git = new GitUtils(repo, project);
//        String mergeCommit = "e34f03bd0c7c805789bdb9da427db7334e61cedc"; // deeplearning4j
//        String mergeCommit = "588def5f5d92ba1e4ec5929dcaed4150a925a90b"; //undertow
//        String mergeCommit = "07559b47674594fdf40f2855f83b492f67f9093c"; //error-prone
        String mergeCommitHash = "0e97a336019b2590a5a486cd4d0249a60db36eb7"; //error-prone 2
        git.checkout(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        String manuallyMergedPath = Utils.saveContent(project, "manualMerge");
        GitCommit targetCommit = git.getTargetMergeCommit(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Perform the merge with the three tools.
        List<Hash> parents = targetCommit.getParents();
        String rightParent = parents.get(0).toShortString();
        String leftParent = parents.get(1).toShortString();
        String baseCommit = git.getBaseCommit(leftParent, rightParent);

        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        boolean isConflicting = gitUtils.merge(rightParent);
        // Add merge commit to database
        MergeCommit mergeCommit = new MergeCommit(mergeCommitHash, isConflicting, leftParent,
                rightParent, proj, targetCommit.getTimestamp());
        mergeCommit.saveIt();

        // Merge the merge scenario with the three tools and record the runtime
        long refMergeRuntime = runRefMerge(project, repo, leftParent, rightParent);
        String refMergePath = Utils.saveContent(project, "refMergeResults");
        // Run GitMerge
        long gitMergeRuntime = runGitMerge(project, repo, leftParent, rightParent);
        String gitMergePath = Utils.saveContent(project, "gitMergeResults");
        // Run IntelliMerge
        String intelliMergePath = System.getProperty("user.home") + "/temp/intelliMergeResults";
 //       long intelliMergeRuntime = runIntelliMerge(project, repo, leftParent, baseCommit, rightParent, intelliMergePath);

        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Integer, List<ConflictingFileData>> refMergeConflicts = EvaluationUtils.extractMergeConflicts(refMergePath);
        Pair<Integer, List<ConflictingFileData>> gitMergeConflicts = EvaluationUtils.extractMergeConflicts(gitMergePath);
//        Pair<Integer, List<ConflictingFile>> intelliMergeConflicts = EvaluationUtils.extractMergeConflicts(intelliMergePath);

        // Format all java files in each directory
        EvaluationUtils.formatAllJavaFiles(manuallyMergedPath);
        EvaluationUtils.formatAllJavaFiles(refMergePath);
//        EvaluationUtils.formatAllJavaFiles(intelliMergePath);
        EvaluationUtils.formatAllJavaFiles(gitMergePath);

        // Get manually merged java files
        File manuallyMergedDir = new File(manuallyMergedPath);
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMergedDir);

        // Compare tools with manually merged code
        ComparisonResult refMergeVsManual = EvaluationUtils.compareAutoMerged(refMergePath, manuallyMergedFiles, project, repo);
        ComparisonResult gitVsManual = EvaluationUtils.compareAutoMerged(gitMergePath, manuallyMergedFiles, project, repo);

        System.out.println("Elapsed RefMerge runtime = " + refMergeRuntime);
        System.out.println("Elapsed Git merge runtime = " + gitMergeRuntime);
//        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);
        System.out.println("Total RefMerge Conflicts: " + refMergeConflicts.getLeft());
        System.out.println("Total Git Merge Conflicts: " + gitMergeConflicts.getLeft());
//        System.out.println("Total IntelliMerge Conflicts: " + intelliMergeConflicts.getLeft());

        System.out.println("RefMerge Statistics:\n#Different Files: " + refMergeVsManual.getTotalDiffFiles() + "\nPrecision: " +
                refMergeVsManual.getPrecision() + "\nRecall: " + refMergeVsManual.getRecall());
        System.out.println("Git Statistics:\n#Different Files: " + gitVsManual.getTotalDiffFiles() + "\nPrecision: " +
                gitVsManual.getPrecision() + "\nRecall: " + gitVsManual.getRecall());


        // Add RefMerge data to database
        MergeResult refMergeResult = new MergeResult("RefMerge", refMergeConflicts.getLeft(), refMergeVsManual.getTotalDiffFiles(),
                refMergeVsManual.getPrecision(), refMergeVsManual.getRecall(), refMergeRuntime, mergeCommit);
        refMergeResult.saveIt();
        // Add conflicting files to database
        List<ConflictingFileData> refMergeConflictingFiles = refMergeConflicts.getRight();
        for(ConflictingFileData conflictingFileData : refMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(refMergeResult, conflictingFileData);
            conflictingFile.saveIt();
        }

            // Add conflict blocks to database

        // Add IntelliMerge data to database
        MergeResult gitMergeResult = new MergeResult("Git", gitMergeConflicts.getLeft(), gitVsManual.getTotalDiffFiles(),
                gitVsManual.getPrecision(), gitVsManual.getRecall(), gitMergeRuntime, mergeCommit);
        gitMergeResult.saveIt();
            // Add conflicting files to database
        List<ConflictingFileData> gitMergeConflictingFiles = gitMergeConflicts.getRight();
        for(ConflictingFileData conflictingFileData : gitMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(gitMergeResult, conflictingFileData);
            conflictingFile.saveIt();
        }
            // Add conflict blocks to database

        // Add Git data to database
            // Add conflicting files to database
            // Add conflict blocks to database

    }

    /*
     * Merge the left and right parent using RefMerge. Return how long it takes for RefMerge to finish
     */
    private long runRefMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        RefMerge refMerging = new RefMerge();
        System.out.println("Starting RefMerge");
        long time = System.currentTimeMillis();
        refMerging.refMerge(leftParent, rightParent, project, repo);
        long time2 = System.currentTimeMillis();
        System.out.println("RefMerge is done");
        return time2 - time;
    }

    /*
     * Merge the left and right parent using Git. Return how long it takes for Git to finish
     */
    private long runGitMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        long time = System.currentTimeMillis();
        gitUtils.merge(rightParent);
        long time2 = System.currentTimeMillis();
        return time2 - time;
    }

    /*
     * Merge the left and right parent using IntelliMerge via command line. Return how long it takes for IntelliMerge
     * to finish
     */
    private long runIntelliMerge(Project project, GitRepository repo, String leftParent, String baseCommit,
                                 String rightParent, String output) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        String leftPath = Utils.saveContent(project, "intelliMerge/ours");
        gitUtils.checkout(baseCommit);
        String basePath = Utils.saveContent(project, "intelliMerge/base");
        gitUtils.checkout(rightParent);
        String rightPath = Utils.saveContent(project, "intelliMerge/theirs");
        String jarFile =  System.getProperty("user.home") + "/temp/IntelliMerge-1.0.7-all.jar";
        System.out.println("Starting IntelliMerge");
        long time = System.currentTimeMillis();
        try {
            Utils.runSystemCommand("java", "-jar", jarFile, "-d", leftPath, basePath, rightPath, "-o", output);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("IntelliMerge is done");
        return time2 - time;
    }

}