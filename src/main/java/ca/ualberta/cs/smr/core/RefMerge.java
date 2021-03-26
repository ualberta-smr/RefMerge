package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import ca.ualberta.cs.smr.utils.Utils;
import ca.ualberta.cs.smr.utils.sortingUtils.SortPairs;
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
import org.refactoringminer.api.RefactoringType;
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
//        String mergeCommit = "27121f2";
//        String rightCommit = "e5e397da6";
//        String leftCommit = "5e59da77";
//        String baseCommit = "c382b804";
//        String mergeCommit = "";
//        String rightCommit = "f42429d";
//        String leftCommit = "1687e13";
//        String baseCommit = "46eeb31";
          // Rajawali
//        String mergeCommit = "98787ef5";
//        String rightCommit = "3d9b713ba";
//        String leftCommit = "5e7fcebe4";
//        String baseCommit = "773d48939a2ccba";
        // Extract Method
        String mergeCommit = "2c959cae5";
        String rightCommit = "2b4f6d9b";
        String leftCommit = "e4270319fd07";
        String baseCommit = "b127f6e2634";

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
        List<Pair> rightRefs = detectCommits(rightCommit, baseCommit);
        SortPairs.sortList(rightRefs);
        List<Pair> leftRefs = detectCommits(leftCommit, baseCommit);
        SortPairs.sortList(leftRefs);

        // Checkout base commit and store it in temp/base
        gitUtils.checkout(baseCommit);
        Utils.dumbServiceHandler(project);
        Utils.saveContent(project, "base");

        gitUtils.checkout(rightCommit);
        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        undoRefactorings(rightRefs);
        Utils.saveContent(project, "right");
        String rightUndoCommit = gitUtils.addAndCommit();
        gitUtils.checkout(leftCommit);

        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        undoRefactorings(leftRefs);
        Utils.saveContent(project, "left");
        String leftUndoCommit = gitUtils.addAndCommit();
        gitUtils.merge(leftUndoCommit, rightUndoCommit);
        Utils.refreshVFS();
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);

        // Check if any of the refactorings are conflicting or have ordering dependencies
        Matrix matrix = new Matrix(project);
        DependenceGraph graph = matrix.runMatrix(leftRefs, rightRefs);

        // Combine the lists so we can perform all the refactorings on the merged project
        // Replay all of the refactorings
        replayRefactorings(graph);


    }

    /*
     * undoRefactorings takes a list of refactorings and performs the inverse for each one.
     */
    public void undoRefactorings(List<Pair> pairs) {
        UndoOperations undo = new UndoOperations(project);
        // Iterate through the list of refactorings and undo each one
        for(Pair pair : pairs) {
            Refactoring ref = pair.getValue();
            switch (ref.getRefactoringType()) {
                case RENAME_CLASS:
                    // Undo the rename class refactoring. This is commented out because of the prompt issue
                    undo.undoRenameClass(ref);
                    break;
                case RENAME_METHOD:
                    // Undo the rename method refactoring
                    undo.undoRenameMethod(ref);
                    break;
                case EXTRACT_OPERATION:
                    undo.undoExtractMethod(ref);
            }

        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

    /*
     * replayRefactorings takes a list of refactorings and performs each of the refactorings.
     */
    public void replayRefactorings(DependenceGraph graph) {
        try {
            ReplayOperations replay = new ReplayOperations(project);
            List<Node> nodes = graph.getSortedNodes();
            for(Node node : nodes) {
                Refactoring ref = node.getRefactoring();
                switch (ref.getRefactoringType()) {
                    case RENAME_CLASS:
                        replay.replayRenameClass(ref);
                        break;
                    case RENAME_METHOD:
                        // Perform the rename method refactoring
                        replay.replayRenameMethod(ref);
                        break;
                    case EXTRACT_OPERATION:
                        replay.replayExtractMethod(ref);
                }

            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

    /*
     * detectCommits uses RefactoringMiner to get the refactorings from commits between the base and commit.
     */
    public List<Pair> detectCommits(String commit, String base) {
        // Store the resulting refactorings into refResult
        List<Pair> refResult = new ArrayList<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            miner.detectBetweenCommits(git.getRepository(), base, commit,
                    new RefactoringHandler() {
                        private int count = 0;
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            // Add each refactoring to refResult
                            for(Refactoring refactoring : refactorings) {
                                RefactoringType type = refactoring.getRefactoringType();
                                if(type == RefactoringType.RENAME_CLASS || type == RefactoringType.RENAME_METHOD
                                        || type == RefactoringType.EXTRACT_OPERATION) {
                                    Pair pair = new Pair(count, refactoring);
                                    refResult.add(pair);
                                }
                            }
                            count++;
                            // For undo, we want to start at the highest count and go to 0
                            // For replay, start at 0 and continue to the highest.
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return refResult;
    }

}