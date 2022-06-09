package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpFieldDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PushDownFieldDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PushDownFieldPullUpFieldCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PushDownFieldPushDownFieldCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PushDownFieldReceiver extends Receiver {

    /*
     * Check for push down field / push down field naming and accidental shadowing conflicts. If on the same branch, check
     * for transitivity.
     */
    @Override
    public void receive(PushDownFieldDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PushDownFieldPushDownFieldCell cell = new PushDownFieldPushDownFieldCell(project);
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
     * Check for push down field / pull up field naming and shadow conflicts
     */
    @Override
    public void receive(PullUpFieldDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PushDownFieldPullUpFieldCell cell = new PushDownFieldPullUpFieldCell(project);
        if(!dispatcher.isSimplify()) {
            boolean isConflicting = cell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }
    }

}
