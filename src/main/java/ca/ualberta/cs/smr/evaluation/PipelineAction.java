package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.model.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.model.ConflictingFile;
import ca.ualberta.cs.smr.evaluation.model.SourceFile;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import ca.ualberta.cs.smr.core.RefMerge;

import java.io.File;
import java.util.*;

public class PipelineAction extends AnAction {

    private Project project;

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.project = ProjectManager.getInstance().getOpenProjects()[0];
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
        System.out.println(repo);
        try {
            runEvaluation(repo);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    private void runEvaluation(GitRepository repo) {
        String clonedDest = this.project.getBasePath();
        assert clonedDest != null;
        GitUtils git = new GitUtils(repo, project);
//        String mergeCommit = "e34f03bd0c7c805789bdb9da427db7334e61cedc"; // deeplearning4j
//        String mergeCommit = "588def5f5d92ba1e4ec5929dcaed4150a925a90b"; //undertow
//        String mergeCommit = "07559b47674594fdf40f2855f83b492f67f9093c"; //error-prone
        String mergeCommit = "0e97a336019b2590a5a486cd4d0249a60db36eb7"; //error-prone 2
        git.checkout(mergeCommit);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        String manuallyMergedPath = Utils.saveContent(project, "manualMerge");
        GitCommit targetCommit = git.getTargetMergeCommit(mergeCommit);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Perform the merge with the three tools.
        List<Hash> parents = targetCommit.getParents();
        String rightParent = parents.get(0).toShortString();
        String leftParent = parents.get(1).toShortString();
        String baseCommit = git.getBaseCommit(leftParent, rightParent);
        // Merge the merge scenario with the three tools and record the runtime
        long refMergeRuntime = runRefMerge(project, repo, leftParent, rightParent);
        String refMergePath = Utils.saveContent(project, "refMergeResults");
        // Run GitMerge
        long gitMergeRuntime = runGitMerge(project, repo, leftParent, rightParent);
        String gitMergePath = Utils.saveContent(project, "gitMergeResults");
        // Run IntelliMerge
        String intelliMergePath = System.getProperty("user.home") + "/temp/intelliMergeResults";
        long intelliMergeRuntime = runIntelliMerge(project, repo, leftParent, baseCommit, rightParent, intelliMergePath);

        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Integer, List<ConflictingFile>> refMergeConflicts = EvaluationUtils.extractMergeConflicts(refMergePath);
        Pair<Integer, List<ConflictingFile>> gitMergeConflicts = EvaluationUtils.extractMergeConflicts(gitMergePath);
        Pair<Integer, List<ConflictingFile>> intelliMergeConflicts = EvaluationUtils.extractMergeConflicts(intelliMergePath);

        // Format all java files in each directory
        EvaluationUtils.formatAllJavaFiles(manuallyMergedPath);
        EvaluationUtils.formatAllJavaFiles(refMergePath);
        EvaluationUtils.formatAllJavaFiles(intelliMergePath);
        EvaluationUtils.formatAllJavaFiles(gitMergePath);

        // Get manually merged java files
        File manuallyMergedDir = new File(manuallyMergedPath);
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMergedDir);

        // Compare tools with manually merged code
        ComparisonResult refMergeVsManual = EvaluationUtils.compareAutoMerged(refMergePath, manuallyMergedFiles);
        System.out.println("Elapsed RefMerge runtime = " + refMergeRuntime);
        System.out.println("Elapsed Git merge runtime = " + gitMergeRuntime);
        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);
        System.out.println("Total RefMerge Conflicts: " + refMergeConflicts.getLeft());
        System.out.println("Total Git Merge Conflicts: " + gitMergeConflicts.getLeft());
        System.out.println("Total IntelliMerge Conflicts: " + intelliMergeConflicts.getLeft());
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