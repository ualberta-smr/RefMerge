import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import refactoring.core.RefMerge;
import utils.GitUtils;

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
            eval.runAnalysis(repo);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runAnalysis(GitRepository repo) throws IOException, GitAPIException, VcsException {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        String clonedDest = project.getBasePath();
        File repoDir = new File(clonedDest);
        GitUtils git = new GitUtils(repo, project);
        // get merge commits from project
        List<GitCommit> mergeCommits = git.getMergeCommits();
        for(GitCommit mergeCommit : mergeCommits) {
            // Run refMerge on projects
            runRefMerge(project, repo, mergeCommit);
            // Run git merge on projects
            // Run IntelliMerge on projects
        }
    }

    private void runRefMerge(Project project, GitRepository repo, GitCommit mergeCommit) throws VcsException, IOException {
        RefMerge refMerging = new RefMerge();
        GitUtils git = new GitUtils(repo, project);
        // Get parent commits
        List<Hash> parents = mergeCommit.getParents();
        String merge = mergeCommit.getId().toString();
        String leftParent = parents.get(0).toString();
        String rightParent = parents.get(1).toString();
        // Get base commit
        String base = git.getBaseCommit(leftParent, rightParent);
        refMerging.refMerge(merge, leftParent, rightParent, base, project, repo);

    }




}