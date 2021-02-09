package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.FileContentUtil;
import git4idea.GitCommit;
import git4idea.GitRevisionNumber;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.reset.GitResetMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitUtils {

    private final Project project;
    private final GitRepository repo;

    public GitUtils(GitRepository repository, Project proj) throws IOException {
        repo = repository;
        project = proj;
    }

    /*
     * Perform the git checkout with the IntelliJ API.
     */
    public void checkout(String commit) throws VcsException {
        GitThread thread = new GitThread(repo, commit);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Refresh the virtual file system after the commit
        VirtualFileManager vFM = VirtualFileManager.getInstance();
        vFM.refreshWithoutFileWatcher(false);
        // Update the PSI classes after the commit
        ArrayList<VirtualFile> vFileCollection = new ArrayList<>();
        File file = new File(project.getBasePath());
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        VirtualFile[] vFileArray = virtualFile.getChildren();
        for(VirtualFile vFile : vFileArray) {
            vFileCollection.add(vFile);
        }
        FileContentUtil.reparseFiles(project, vFileCollection, true);
    }

    /*
     * Get the base commit of the merge.
     */
    public String getBaseCommit(String left, String right) throws VcsException {
        VirtualFile root = repo.getRoot();
        GitRevisionNumber num = GitHistoryUtils.getMergeBase(project, root, left, right);
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
        GitCommandResult res = Git.getInstance().checkout(repo, commit, null, true, false, false);
        System.out.println(res);
    }
}
