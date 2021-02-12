package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.api.Git;
import ca.ualberta.cs.smr.utils.GitUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RefMerge extends AnAction {

    Git git;
    Project project;

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }
    // Example: multiple rename methods
    // Project: core
    // URL: https://github.com/MasDennis/Rajawali
    // merge commit: 98787ef5
    // parent 1: 3d9b713ba
    // parent 2: 5e7fcebe4
    // base: 773d48939a2ccba
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.project = ProjectManager.getInstance().getOpenProjects()[0];
        Project project = this.project;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
        String mergeCommit = "27121f2";//"98787ef5";
        String rightCommit = "e5e397da6"; //"3d9b713ba";
        String leftCommit = "5e59da77"; //"5e7fcebe4";
        String baseCommit = "c382b804"; //"773d48939a2ccba";

        refMerge(mergeCommit, rightCommit, leftCommit, baseCommit, project, repo);

    }

    /*
     * Gets the directory of the project that's being merged, then it calls the function that performs the merge.
     */
    public void refMerge(String mergeCommit, String rightCommit, String leftCommit, String baseCommit, Project project,
                         GitRepository repo) {
        Utils.clearTemp();
        File dir = new File(Objects.requireNonNull(project.getBasePath()));
        try {
            git = Git.open(dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        doMerge(rightCommit, leftCommit, baseCommit, repo);

    }

    /*
     * This method gets the refactorings that are between the base commit and the left and right commits. It uses the
     * matrix to determine if any of the refactorings are conflicting or have ordering dependencies.
     * Then it checks out the base commit, saving it in a temporary directory. It checks out the right commit, undoes
     * the refactorings, and saves the content into a respective temporary directory. It does the same thing for the
     * left commit, but it uses the current directory instead of saving it to a new one. After it's undone all the
     * refactorings, the merge function is called and it replays the refactorings.
     */
    private void doMerge(String rightCommit, String leftCommit, String baseCommit,
                         GitRepository repo) {

        GitUtils gitUtils = new GitUtils(repo, project);
        // Detect the right refactorings and store them in a list
        List<Refactoring> rightRefs = detectCommits(rightCommit, baseCommit);
        // Detect the left refactorings and store them in a list
        List<Refactoring> leftRefs = detectCommits(leftCommit, baseCommit);
        // Check if any of the refactorings are conflicting or have ordering dependencies
        Matrix matrix = new Matrix(project.getBasePath());
        matrix.runMatrix(leftRefs, rightRefs);

        // Checkout base commit and store it in temp/base
        gitUtils.checkout(baseCommit);
        Utils.dumbServiceHandler(project);
        Utils.saveContent(project, "base");

        gitUtils.checkout(rightCommit);
        // Update the PSI classes after the commit
        Utils.reparsePSIClasses(project);
        Utils.dumbServiceHandler(project);
        undoRefactorings(rightRefs);
        Utils.saveContent(project, "right");

        // Breaks here?
        gitUtils.checkout(leftCommit);
        // Update the PSI classes after the commit
        Utils.reparsePSIClasses(project);
        Utils.dumbServiceHandler(project);
        undoRefactorings(leftRefs);
        Utils.saveContent(project, "left");

        // Merge the left and right commit now that there are no refactorings
        Merge merge = new Merge(project);
        merge.merge();

        // Combine the lists so we can perform all the refactorings on the merged project
        leftRefs.addAll(rightRefs);
        // Replay all of the refactorings
        replayRefactorings(leftRefs);


    }

    /*
     * undoRefactorings takes a list of refactorings and performs the inverse for each one.
     */
    public void undoRefactorings(List<Refactoring> refs) {
        UndoOperations undo = new UndoOperations(project);

        // Iterate through the list of refactorings and undo each one
        for(Refactoring ref : refs) {
            switch (ref.getRefactoringType()) {
                case RENAME_CLASS:
                    // Undo the rename class refactoring. This is commented out because of the prompt issue
                    undo.undoRenameClass(ref);
                    break;
                case RENAME_METHOD:
                    // Undo the rename method refactoring
                    undo.undoRenameMethod(ref);
                    break;
            }

        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

    /*
     * replayRefactorings takes a list of refactorings and performs each of the refactorings.
     */
    public void replayRefactorings(List<Refactoring> refs) {
        try {
            ReplayOperations replay = new ReplayOperations(project);
            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_CLASS:
                        break;
                    case RENAME_METHOD:
                        // Perform the rename method refactoring
                        replay.replayRenameMethod(ref);
                        break;
                }

            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

    /*
     * detectCommits uses RefactoringMiner to get the commits between the base and commit.
     */
    public List<Refactoring> detectCommits(String commit, String base) {
        // Store the resulting refactorings into refResult
        List<Refactoring> refResult = new ArrayList<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            miner.detectBetweenCommits(git.getRepository(), base, commit,
                    new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            // Add each refactoring to refResult
                            refResult.addAll(refactorings);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return refResult;
    }

}