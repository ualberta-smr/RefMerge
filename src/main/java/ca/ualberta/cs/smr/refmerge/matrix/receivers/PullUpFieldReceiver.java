package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpFieldDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PullUpFieldPullUpFieldCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PullUpFieldReceiver extends Receiver {

    /*
     * Check for pull up field / pull up field naming and accidental shadowing conflicts. If on the same branch, check
     * for transitivity.
     */
    @Override
    public void receive(PullUpFieldDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        PullUpFieldPullUpFieldCell cell = new PullUpFieldPullUpFieldCell(project);
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

}
