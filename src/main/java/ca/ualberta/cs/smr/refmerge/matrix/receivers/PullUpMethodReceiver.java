package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.PullUpMethodPullUpMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class PullUpMethodReceiver extends Receiver {

    /*
     * Checks for pull up method/pull up method conflicts
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {

        // No simplification to be done here
        if(dispatcher.isSimplify()) {
            return;
        }

        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            PullUpMethodPullUpMethodCell pullUpMethodPullUpMethodCell = new PullUpMethodPullUpMethodCell(project);
            boolean isConflicting = pullUpMethodPullUpMethodCell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }

    }
}
