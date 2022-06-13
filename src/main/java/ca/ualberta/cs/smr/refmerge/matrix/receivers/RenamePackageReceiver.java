package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenamePackageDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenamePackageExtractMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenamePackageMoveRenameMethodCell;
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

    /*
     * If simplify is true, check for rename package / move + rename method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

}
