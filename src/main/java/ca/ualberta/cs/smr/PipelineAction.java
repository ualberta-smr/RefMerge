package ca.ualberta.cs.smr;

import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import ca.ualberta.cs.smr.core.RefMerge;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PipelineAction extends AnAction {



    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PipelineAction eval = new PipelineAction();
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
       System.out.println(repo);
        try {
            eval.runEvaluation(repo);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    private void runEvaluation(GitRepository repo) throws IOException, GitAPIException, VcsException {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        String clonedDest = project.getBasePath();
        assert clonedDest != null;
        File repoDir = new File(clonedDest);
        GitUtils git = new GitUtils(repo, project);
        String mergeCommit = "e34f03bd0c7c805789bdb9da427db7334e61cedc";
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
                List<Hash> parents = gitCommit.getParents();
                String leftParent = parents.get(0).toShortString();
                String rightParent = parents.get(1).toShortString();
                String baseCommit = git.getBaseCommit(leftParent, rightParent);
                runRefMerge(project, repo, leftParent, rightParent);
            }
        }



    }

    private void runRefMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        RefMerge refMerging = new RefMerge();
        refMerging.refMerge(leftParent, rightParent, project, repo);
    }




}