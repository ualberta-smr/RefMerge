package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitCommit;
import git4idea.GitRevisionNumber;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.reset.GitResetMode;

import java.util.ArrayList;
import java.util.List;

public class GitUtils {

    private final Project project;
    private final GitRepository repo;

    public GitUtils(GitRepository repository, Project proj) {
        repo = repository;
        project = proj;
    }

    /*
     * Perform the git checkout with the IntelliJ API.
     */
    public void checkout(String commit) {
        GitThread thread = new GitThread(repo, commit);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Refresh the virtual file system after the commit
        Utils.refreshVFS();
    }

    /*
     * Perform git add -A and git commit
     */
    public String addAndCommit() {
        add();
        return commit();
    }

    public void add() {
        GitAdd thread = new GitAdd(project, repo);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String commit() {
        DoGitCommit gitCommit = new DoGitCommit(repo, project);

        Thread thread = new Thread(gitCommit);
        thread.start();
        try {
            thread.join();
            return gitCommit.getCommit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void merge(String leftCommit, String rightCommit) {
        Thread thread = new Thread(() -> {
            GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.MERGE);
            lineHandler.addParameters(leftCommit, rightCommit, "--no-commit");
            Git.getInstance().runCommand(lineHandler);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.MERGE);
//        lineHandler.addParameters(leftCommit, rightCommit, "--no-commit");
//        Git.getInstance().runCommand(lineHandler);
    }

    /*
     * Get the base commit of the merge.
     */
    public String getBaseCommit(String left, String right) throws VcsException {
        VirtualFile root = repo.getRoot();
        GitRevisionNumber num = GitHistoryUtils.getMergeBase(project, root, left, right);
        assert num != null;
        return num.getRev();
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

class DoGitCommit implements Runnable {
    private final Project project;
    private final GitRepository repo;
    private String commit;

    public DoGitCommit(GitRepository repo, Project project) {
        this.project = project;
        this.repo = repo;
    }

    @Override
    public void run() {
        GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.COMMIT);
        // Add message to commit to clearly show it's RefMerge step
        lineHandler.addParameters("-m", "RefMerge");
        GitCommandResult result = Git.getInstance().runCommand(lineHandler);
        String res = result.getOutputAsJoinedString();
        // get the commit hash from the output message
        String commit = res.substring(res.indexOf("[") + 1, res.indexOf("]") - 1);
        this.commit = commit.substring(commit.lastIndexOf(" ") + 1);
    }

    public String getCommit() {
        return commit;
    }
}

class GitAdd extends Thread {
    final GitRepository repo;
    final Project project;

    public GitAdd(Project project, GitRepository repo) {
        this.project = project;
        this.repo = repo;
    }

    @Override
    public void run() {
        GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.ADD);
        lineHandler.addParameters("-A");
        Git.getInstance().runCommand(lineHandler);
    }

}

class GitThread extends Thread {
    final GitRepository repo;
    final String commit;

    public GitThread(GitRepository repo, String commit) {
        this.repo = repo;
        this.commit = commit;
    }

    @Override
    public void run()
    {
        Git.getInstance().reset(repo, GitResetMode.HARD, "HEAD");
        Git.getInstance().checkout(repo, commit, null, true, false, false);
    }
}
