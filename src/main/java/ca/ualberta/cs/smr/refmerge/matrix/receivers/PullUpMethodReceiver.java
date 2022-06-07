package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.*;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PullUpMethodReceiver extends Receiver {

    /*
     * Checks for pull up method/pull up method conflicts and transitivity
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PullUpMethodPullUpMethodCell cell = new PullUpMethodPullUpMethodCell(project);
        if(dispatcher.isSimplify()) {
            // Dispatcher refactoring is always the second refactoring when dealing with two refactorings of the same type
            this.isTransitive = cell.checkTransitivity(this.refactoringObject, dispatcherRefactoring);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
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

    /*
     * Check for pull up method/extract method conflicts and combination
     */
    public void receive(ExtractMethodDispatcher dispatcher) {
        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(project);
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            cell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
        else {
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }

    }

    public void receive(InlineMethodDispatcher dispatcher) {
        PullUpMethodInlineMethodCell cell = new PullUpMethodInlineMethodCell(project);
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        // No combination to be done at this time
        if(!dispatcher.isSimplify()) {
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }

    }

    public void receive(MoveRenameClassDispatcher dispatcher) {
        PullUpMethodMoveRenameClassCell cell = new PullUpMethodMoveRenameClassCell(project);
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            cell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
        // No conflicts possible between class + method levels

    }

}
