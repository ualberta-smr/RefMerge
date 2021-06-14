package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.core.replayOperations.ReplayOperations;
import ca.ualberta.cs.smr.core.undoOperations.UndoOperations;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.project = ProjectManager.getInstance().getOpenProjects()[0];
        Project project = this.project;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
        String mergeCommit = "efd3ce4f8fa";
        String rightCommit = "38602965d";
        String leftCommit = "2462eb2c79";


        refMerge(mergeCommit, rightCommit, leftCommit, project, repo);

    }

    /*
     * Gets the directory of the project that's being merged, then it calls the function that performs the merge.
     */
    public void refMerge(String mergeCommit, String rightCommit, String leftCommit, Project project,
                         GitRepository repo) {
        Utils.clearTemp();
        File dir = new File(Objects.requireNonNull(project.getBasePath()));
        try {
            git = Git.open(dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        doMerge(rightCommit, leftCommit, repo);

    }

    /*
     * This method gets the refactorings that are between the base commit and the left and right commits. It uses the
     * matrix to determine if any of the refactorings are conflicting or have ordering dependencies.
     * Then it checks out the base commit, saving it in a temporary directory. It checks out the right commit, undoes
     * the refactorings, and saves the content into a respective temporary directory. It does the same thing for the
     * left commit, but it uses the current directory instead of saving it to a new one. After it's undone all the
     * refactorings, the merge function is called and it replays the refactorings.
     */
    private void doMerge(String rightCommit, String leftCommit, GitRepository repo){

        GitUtils gitUtils = new GitUtils(repo, project);
        String baseCommit = gitUtils.getBaseCommit(leftCommit, rightCommit);
        ArrayList<RefactoringObject> rightRefs = getAndSimplifyRefactorings(rightCommit, baseCommit);
        ArrayList<RefactoringObject> leftRefs = getAndSimplifyRefactorings(leftCommit, baseCommit);

        // Checkout base commit and store it in temp/base
        gitUtils.checkout(baseCommit);
        Utils.dumbServiceHandler(project);
        Utils.saveContent(project, "base");

        gitUtils.checkout(rightCommit);
        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        rightRefs = undoRefactorings(rightRefs);
        Utils.saveContent(project, "right");
        String rightUndoCommit = gitUtils.addAndCommit();
        gitUtils.checkout(leftCommit);

        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        leftRefs = undoRefactorings(leftRefs);
        Utils.saveContent(project, "left");
        gitUtils.addAndCommit();
        gitUtils.merge(rightUndoCommit);
        Utils.refreshVFS();
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);

        // Check if any of the refactorings are conflicting or have ordering dependencies
        Matrix matrix = new Matrix(project);
        ArrayList<RefactoringObject> mergedRefactoringList = matrix.runMatrix(leftRefs, rightRefs);

        // Combine the lists so we can perform all the refactorings on the merged project
        // Replay all of the refactorings
        replayRefactorings(mergedRefactoringList);


    }

    /*
     * undoRefactorings takes a list of refactorings and performs the inverse for each one.
     */
    public ArrayList<RefactoringObject> undoRefactorings(ArrayList<RefactoringObject> refactoringObjects) {
        UndoOperations undo = new UndoOperations(project);
        // Iterate through the list of refactorings and undo each one
        for(RefactoringObject refactoringObject : refactoringObjects) {
            switch (refactoringObject.getRefactoringType()) {
                case RENAME_CLASS:
                    // Undo the rename class refactoring. This is commented out because of the prompt issue
                    undo.undoMoveRenameClass(refactoringObject);
                    break;
                case RENAME_METHOD:
                case MOVE_OPERATION:
                case MOVE_AND_RENAME_OPERATION:
                    // Undo the rename method refactoring
                    undo.undoMoveRenameMethod(refactoringObject);
                    break;
                case EXTRACT_OPERATION:
                    refactoringObject = undo.undoExtractMethod(refactoringObject);
                    int index = refactoringObjects.indexOf(refactoringObject);
                    refactoringObjects.set(index, refactoringObject);

            }

        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
        return refactoringObjects;
    }

    /*
     * replayRefactorings takes a list of refactorings and performs each of the refactorings.
     */
    public void replayRefactorings(ArrayList<RefactoringObject> refactoringObjects) {
        try {
            ReplayOperations replay = new ReplayOperations(project);
            for(RefactoringObject refactoringObject : refactoringObjects) {
                switch (refactoringObject.getRefactoringType()) {
                    case RENAME_CLASS:
                        replay.replayMoveRenameClass(refactoringObject);
                        break;
                    case RENAME_METHOD:
                    case MOVE_OPERATION:
                    case MOVE_AND_RENAME_OPERATION:
                        // Perform the rename method refactoring
                        replay.replayMoveRenameMethod(refactoringObject);
                        break;
                    case EXTRACT_OPERATION:
                        replay.replayExtractMethod(refactoringObject);
                }

            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

    /*
     * Use RefMiner to detect refactorings in commits between the base commit and the parent commit. Compare each newly
     * detected refactoring against previously detected refactorings to check for transitivity or if the refactorings can
     * be simplified.
     */
    public ArrayList<RefactoringObject> getAndSimplifyRefactorings(String commit, String base) {
        ArrayList<RefactoringObject> simplifiedRefactorings = new ArrayList<>();
        Matrix matrix = new Matrix(project);
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            miner.detectBetweenCommits(git.getRepository(), base, commit,
                new RefactoringHandler() {
                    @Override
                    public void handle(String commitId, List<Refactoring> refactorings) {
                        // Add each refactoring to refResult
                        for(Refactoring refactoring : refactorings) {
                            // Create the refactoring object so we can compare and update
                            RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(refactoring);
                            // If the refactoring type is not presently supported, skip it
                            if(refactoringObject == null) {
                                continue;
                            }
                            // simplify refactorings and check if factoring is transitive
                            matrix.simplifyAndInsertRefactorings(refactoringObject, simplifiedRefactorings);
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simplifiedRefactorings;
    }

}