package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.vcs.log.Hash;
//import edu.pku.intellimerge.client.APIClient;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
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
//        PipelineAction eval = new PipelineAction();
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
        File repoDir = new File(clonedDest);
        GitUtils git = new GitUtils(repo, project);
//        String mergeCommit = "e34f03bd0c7c805789bdb9da427db7334e61cedc"; // deeplearning4j
//        String mergeCommit = "588def5f5d92ba1e4ec5929dcaed4150a925a90b"; //undertow
        String mergeCommit = "07559b47674594fdf40f2855f83b492f67f9093c";
        git.checkout(mergeCommit);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        Utils.saveContent(project, "manualMerge");
        List<GitCommit> mergeCommits = git.getMergeCommits();
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        for(GitCommit gitCommit : mergeCommits) {
            String commitHash = gitCommit.getId().toString();
            if(commitHash.equals(mergeCommit)) {
                // Perform the merge with the three tools.
                List<Hash> parents = gitCommit.getParents();
                String leftParent = parents.get(0).toShortString();
                String rightParent = parents.get(1).toShortString();
                String baseCommit = git.getBaseCommit(leftParent, rightParent);
                long refMergeRuntime = runRefMerge(project, repo, leftParent, rightParent);
//                long intelliMergeRuntime = runIntelliMerge(git, mergeCommit, leftParent, rightParent,
//                                                            baseCommit, repo.getPresentableUrl());
                System.out.println("Elapsed RefMerge runtime = " + refMergeRuntime);
//                System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);

            }
        }



    }

    /*
     * Merge the left and right parent using RefMerge. Return how long it takes for RefMerge to finish
     */
    private long runRefMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        RefMerge refMerging = new RefMerge();
        long time = System.currentTimeMillis();
        refMerging.refMerge(leftParent, rightParent, project, repo);
        long time2 = System.currentTimeMillis();
        Utils.saveContent(project, "refMergeResults");
        return time2 - time;
    }

    /*
     * Merge the directories in the order <left> <base> <right> with IntelliMerge. Return how long it takes for IntelliMerge
     * to finish.
     */
//    private long runIntelliMerge(GitUtils git, String mergeCommit, String leftParent, String rightParent, String baseCommit,
//                                 String url ) {
//        git.checkout(leftParent);
//        List<String> directories = new ArrayList<>();
//        git.checkout(mergeCommit);
//        String path = System.getProperty("user.home") + "/temp/" + project.getName();
//        Utils.saveContent(project, project.getName() + "/ours/");
//        git.checkout(baseCommit);
//        Utils.saveContent(project, project.getName() + "/base/");
//        git.checkout(rightParent);
//        Utils.saveContent(project, project.getName() + "/theirs/");
//        String outputPath = System.getProperty("user.home") + "/temp/intelliMergeResults";
//        APIClient apiClient = new APIClient(project.getName(), path + "/temp", url, outputPath + "/temp", outputPath, true);
//        try {
//            long time = System.currentTimeMillis();
//            apiClient.processDirectory(path, outputPath);
//            long time2 = System.currentTimeMillis();
//            return time2 - time;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return -1;
//    }



}