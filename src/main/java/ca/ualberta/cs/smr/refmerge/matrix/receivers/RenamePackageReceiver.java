package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenamePackageDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenamePackageRenamePackageCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class RenamePackageReceiver extends Receiver {

    /*
     * If simplify is true, check for rename package / rename package transitivity. If it is false, check for
     * rename package / rename package conflict.
     */
    @Override
    public void receive(RenamePackageDispatcher dispatcher) {
        // Check for rename package / rename package transitivity
        if(dispatcher.isSimplify()) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            // Dispatcher refactoring is always the second refactoring when dealing with two refactorings of the same type
            this.isTransitive = RenamePackageRenamePackageCell.checkTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for rename package / rename package conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            boolean isConflicting = RenamePackageRenamePackageCell
                    .conflictCell(dispatcherRefactoring, this.refactoringObject);
            this.isConflicting = isConflicting;
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }

        }
    }

}
