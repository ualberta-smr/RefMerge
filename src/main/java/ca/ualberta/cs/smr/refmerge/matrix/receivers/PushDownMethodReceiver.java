package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PushDownMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PushDownMethodReceiver extends Receiver {

    @Override
    public void receive(PushDownMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PushDownMethodPushDownMethodCell cell = new PushDownMethodPushDownMethodCell(project);
        if(dispatcher.isSimplify()) {
            // Dispatcher refactoring is always the second refactoring when dealing with two refactorings of the same type
            this.isTransitive = cell.checkTransitivity(this.refactoringObject, dispatcherRefactoring);
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

    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PushDownMethodPullUpMethodCell cell = new PushDownMethodPullUpMethodCell(project);
        if(!dispatcher.isSimplify()) {
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }
    }

    /*
     * Checks for push down method/ move + rename Method conflicts and simplification
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        PushDownMethodMoveRenameMethodCell cell = new PushDownMethodMoveRenameMethodCell(project);
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            cell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }

    }

    /*
     * Check for push up method/extract method conflicts and combination
     */
    public void receive(ExtractMethodDispatcher dispatcher) {
        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(project);
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


}
