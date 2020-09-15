package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.sun.istack.NotNull;
import git4idea.GitCommit;
import git4idea.GitRevisionNumber;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitUtils {

    private Project project;
    private GitRepository repo;

    public GitUtils(GitRepository repository, Project proj) throws IOException {
        repo = repository;
        project = proj;
    }

    /*
     * Use IntelliJ API to perform git reset.
     */
    public void gitReset() {
        ProgressManager.getInstance().run(new Task.Modal(project, "Git Reset", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GitLineHandler resetHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.RESET);
                resetHandler.setSilent(true);
                resetHandler.addParameters("--hard", "HEAD");
                String result = null;
                try {
                    result = git4idea.commands.Git.getInstance().runCommand(resetHandler).getOutputOrThrow();
                } catch (VcsException e) {
                    e.printStackTrace();
                }
                // If there's a lock, remove it and try again
                if (result.contains(".git/index.lock")) {
                    Utils.runSystemCommand(project.getBasePath(),
                            "rm", ".git/index.lock");
                    git4idea.commands.Git.getInstance().runCommand(resetHandler);

                }
            }
        });

    }

    /*
     * Perform the git checkout with the IntelliJ API.
     */
    public void checkout(String commit) throws VcsException {
        gitReset();
        ProgressManager.getInstance().run(new Task.Modal(project, "Git Checkout", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.CHECKOUT);
                lineHandler.setSilent(true);
                lineHandler.addParameters(commit);
                git4idea.commands.Git.getInstance().runCommand(lineHandler);

            }
        });
        // Refresh the virtual file system after the commit
        VirtualFileManager vFM = VirtualFileManager.getInstance();
        vFM.refreshWithoutFileWatcher(false);
    }

    /*
     * Get the base commit of the merge.
     */
    public String getBaseCommit(String left, String right) throws VcsException {
        VirtualFile root = repo.getRoot();
        GitRevisionNumber num = GitHistoryUtils.getMergeBase(project, root, left, right);
        String base = num.getRev();
        return base;
    }

    /*
     * Get the merge commits of a project for evaluation.
     */
    public List<GitCommit> getMergeCommits() throws VcsException {
        // get list of commits
        VirtualFile root = repo.getRoot();
        List<GitCommit> commits = GitHistoryUtils.history(project, root);
        List<GitCommit> mergeCommits = new ArrayList<>();
        System.out.println(commits.size());
        for(GitCommit commit : commits) {
            // check if each commit is a merge commit
            if(commit.getParents().size() == 2) {
                // if yes, add to a list of merge commits
                mergeCommits.add(commit);
            }
        }
        return mergeCommits;
    }

}
