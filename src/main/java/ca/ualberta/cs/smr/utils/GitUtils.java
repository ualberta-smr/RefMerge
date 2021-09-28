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
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GitUtils {

    public static final String CONFLICT_LEFT_BEGIN = "<<<<<<<";
    public static final String CONFLICT_RIGHT_END = ">>>>>>>";

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

    public void reset() {
        Utils.runSystemCommand("git", "clean");
        Git.getInstance().reset(repo, GitResetMode.HARD, "HEAD");
    }

    public boolean merge(String rightCommit) {
        AtomicReference<GitCommandResult> gitCommandResult = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.MERGE);
            lineHandler.addParameters(rightCommit, "--no-commit");
            gitCommandResult.set(Git.getInstance().runCommand(lineHandler));
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String gitMergeResult = gitCommandResult.get().toString();
        if(gitMergeResult.contains("conflict")) {
            return hasJavaConflict(gitMergeResult);
        }
        return false;
    }

    private boolean hasJavaConflict(String gitResult) {
        for (String line : gitResult.split(",")) {
            if (!line.contains("CONFLICT")) {
                continue;
            }
            if (line.contains(".java")) {
                return true;
            }
        }
        return false;
    }

    /*
     * Get the base commit of the merge scenario.
     */
    public String getBaseCommit(String left, String right) {
        VirtualFile root = repo.getRoot();
        class BaseThread extends Thread {
            private final Project project;
            private final VirtualFile root;
            private final String leftCommit;
            private final String rightCommit;
            private String baseCommit;

            BaseThread(Project project, VirtualFile root, String leftCommit, String rightCommit) {
                this.project = project;
                this.root = root;
                this.leftCommit = leftCommit;
                this.rightCommit = rightCommit;
            }
            @Override
            public void run() {
                GitRevisionNumber num = null;
                try {
                    num = GitHistoryUtils.getMergeBase(project, root, leftCommit, rightCommit);
                } catch (VcsException e) {
                    System.out.println("Project: " + project + " LeftCommit: " + leftCommit + " RightCommit: " + rightCommit);
                    e.printStackTrace();
                }
                if(num == null) {
                    this.baseCommit = null;
                }
                else {
                    this.baseCommit = num.getShortRev();
                }
            }

            public String getBaseCommit() {
                return baseCommit;
            }
        }

        BaseThread thread = new BaseThread(project, root, left, right);
        thread.start();
        try {
            thread.join();
            return thread.getBaseCommit();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    /*
     * Get the merge commits of a project for evaluation.
     */
    public List<GitCommit> getMergeCommits() {
        VirtualFile root = repo.getRoot();
        class MergeCommitsThread extends Thread {
            List<GitCommit> commits;
            @Override
            public void run() {
                try {

                    this.commits = GitHistoryUtils.history(project, root);
                } catch (VcsException e) {
                    e.printStackTrace();
                }
            }

            public List<GitCommit> getCommits() {
                return this.commits;
            }
        }
        MergeCommitsThread thread = new MergeCommitsThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<GitCommit> commits = thread.getCommits();
        // get list of commits
        List<GitCommit> mergeCommits = new ArrayList<>();
        for(GitCommit commit : commits) {
            // check if each commit is a merge commit
            if(commit.getParents().size() == 2) {
                // if yes, add to a list of merge commits
                mergeCommits.add(commit);
            }
        }
        return mergeCommits;
    }

    /*
     * Get the target merge commit that we are evaluating the tools on.
     */
    public GitCommit getTargetMergeCommit(String targetCommit) {
        GitCommit mergeCommit = null;
        List<GitCommit> mergeCommits = this.getMergeCommits();
        for(GitCommit gitCommit : mergeCommits) {
            String commitHash = gitCommit.getId().toString();
            if (commitHash.equals(targetCommit)) {
                mergeCommit = gitCommit;
            }
        }
        return mergeCommit;
    }

    public static String diff(String dir, String path1, String path2) {
        StringBuilder builder = new StringBuilder();
        try {
            String commands = "git diff --ignore-cr-at-eol --ignore-all-space --ignore-blank-lines --ignore-space-change " +
                    "--no-index -U0 " + path1 + " " + path2;
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands, null, new File(dir));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
            }
            while ((s = stdError.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();

    }

    /*
     * Checkout the given commit for the IntelliMerge replication. Use this instead of GitUtils because there is no project.
     */
    public static void checkoutForReplication(org.eclipse.jgit.api.Git git, String commit) throws GitAPIException {
        git.checkout().setName(commit).call();
    }

    /*
     * Get the base commit for the IntelliMerge replications. Use this instead of GitUtils because there is no project.
     */
    public static String getBaseCommit(RevCommit leftParent, RevCommit rightParent, Repository repository) throws IOException {
        RevWalk walk = new RevWalk(repository);
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(leftParent);
        walk.markStart(rightParent);
        RevCommit mergeBase = walk.next();
        if(mergeBase == null) {
            return null;
        }
        return mergeBase.getName();
    }

    /*
     * Reset for IntelliMerge replication
     */
    public static void gitReset(org.eclipse.jgit.api.Git git) throws GitAPIException {
        ResetCommand reset = git.reset();
        reset.setRef("HEAD");
        reset.setMode(ResetCommand.ResetType.HARD);
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        String lockPath = git.getRepository().getWorkTree().getAbsolutePath() + ".git/index.lock";
        File f = new File(lockPath);
        if (f.exists()) {
            Utils.runSystemCommand("rm", lockPath);
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
        }
    }

    /*
     * Get all merge scenarios for merge scenario collection
     */
    public static Iterable<RevCommit> getMergeScenarios(org.eclipse.jgit.api.Git git) {
        try {
        gitReset(git);
        return git.log().all().setRevFilter(new RevFilter() {
            @Override
            public boolean include(RevWalk revWalk, RevCommit revCommit) throws StopWalkException {
                return revCommit.getParentCount() == 2;
            }

            @Override
            public RevFilter clone() {
                return this;
            }
        }).call();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getConflictingFilePaths() {
        AtomicReference<GitCommandResult> gitCommandResult = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            GitLineHandler lineHandler = new GitLineHandler(project, repo.getRoot(), GitCommand.DIFF);
            lineHandler.addParameters("--name-only", "--diff-filter=U");
            gitCommandResult.set(Git.getInstance().runCommand(lineHandler));
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gitCommandResult.get().getOutput();

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
        String res = result.getOutput().get(0);
        // get the commit hash from the output message
        String commit;
        if(res.contains("]")) {
            commit = res.substring(res.indexOf("HEAD") + 1, res.indexOf("]") - 1);
        }
        else {
            commit = res.substring(res.lastIndexOf(" ") + 1, res.length()-1);
        }
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
