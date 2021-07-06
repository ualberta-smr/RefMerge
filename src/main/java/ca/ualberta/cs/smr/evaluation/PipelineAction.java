package ca.ualberta.cs.smr.evaluation;

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
        String mergeCommit = "588def5f5d92ba1e4ec5929dcaed4150a925a90b"; //undertow
//        String mergeCommit = "07559b47674594fdf40f2855f83b492f67f9093c"; //error-prone
        git.checkout(mergeCommit);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        Utils.saveContent(project, "manualMerge");
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
        long intelliMergeRuntime = runIntelliMerge(project, repo, leftParent, baseCommit, rightParent);

        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Integer, String> refMergeConflicts = EvaluationUtils.extractMergeConflicts(refMergePath);
        System.out.println("Elapsed RefMerge runtime = " + refMergeRuntime);
        System.out.println("Elapsed Git merge runtime = " + gitMergeRuntime);
        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);
    }

    /*
     * Merge the left and right parent using RefMerge. Return how long it takes for RefMerge to finish
     */
    private long runRefMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        RefMerge refMerging = new RefMerge();
        long time = System.currentTimeMillis();
        refMerging.refMerge(leftParent, rightParent, project, repo);
        long time2 = System.currentTimeMillis();
        return time2 - time;
    }

    /*
     * Merge the left and right parent using Git. Return how long it takes for Git to finish
     */
    private long runGitMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        long time = System.currentTimeMillis();
        Utils.runSystemCommand("git", "merge", rightParent);
        long time2 = System.currentTimeMillis();
        return time2 - time;
    }

    /*
     * Merge the left and right parent using IntelliMerge via command line. Return how long it takes for IntelliMerge
     * to finish
     */
    private long runIntelliMerge(Project project, GitRepository repo, String leftParent, String baseCommit, String rightParent) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        String leftPath = Utils.saveContent(project, "intelliMerge/ours");
        gitUtils.checkout(baseCommit);
        String basePath = Utils.saveContent(project, "intelliMerge/base");
        gitUtils.checkout(rightParent);
        String rightPath = Utils.saveContent(project, "intelliMerge/theirs");
        long time = System.currentTimeMillis();
        String jarFile =  System.getProperty("user.home") + "/temp/IntelliMerge-1.0.7-all.jar";
        String output = System.getProperty("user.home") + "/temp/intelliMergeResults";
        System.out.println(jarFile);
        Utils.runSystemCommand("java", "-jar", jarFile, "-d", leftPath, basePath, rightPath, "-o", output);
        long time2 = System.currentTimeMillis();
        return time2 - time;
    }

}