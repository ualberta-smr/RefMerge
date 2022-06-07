package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PullUpMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PullUpMethodPullUpMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PullUpMethodReceiver extends Receiver {

    /*
     * Checks for pull up method/pull up method conflicts
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PullUpMethodPullUpMethodCell cell = new PullUpMethodPullUpMethodCell(project);
        if(dispatcher.isSimplify()) {
            // Dispatcher refactoring is always the second refactoring when dealing with two refactorings of the same type
            this.isTransitive = cell.checkTransitivity(this.refactoringObject, dispatcherRefactoring);
        }

        if(!dispatcher.isSimplify()) {
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }
    }

    /*
     * Checks for pull up method/MoveRenameMethod conflicts and simplification
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {

        // No simplification in this case
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(project);
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            cell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(project);
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }

    }



}
