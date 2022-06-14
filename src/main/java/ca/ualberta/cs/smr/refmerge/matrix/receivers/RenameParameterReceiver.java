package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenamePackageDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenameParameterDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenameParameterRenamePackageCell;
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

    /*
     * If simplify is true, check for rename parameter / rename package combination. If it is false, there is no
     * conflict to check for between package and parameter level
     */
    @Override
    public void receive(RenamePackageDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            RenameParameterRenamePackageCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

}
