package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenameParameterDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenameParameterRenameParameterCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class RenameParameterReceiver extends Receiver {

    /*
     * If performing a same branch comparison, check for rename parameter / rename parameter transitivity.
     * If performing cross-branch comparison, check for naming conflicts.
     */
    @Override
    public void receive(RenameParameterDispatcher dispatcher) {
        RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
        // Check for transitivity
        if(dispatcher.isSimplify()) {
            this.isTransitive = RenameParameterRenameParameterCell.checkTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for naming conflicts
        else {
            boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(secondRefactoring, this.refactoringObject);
            if(isConflicting) {
                this.isConflicting = true;
                secondRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }
    }

}
