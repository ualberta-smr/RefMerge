package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.*;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenameFieldMoveRenameClassCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.RenameFieldRenameFieldCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;


public class MoveRenameFieldReceiver extends Receiver {

    @Override
    public void receive(MoveRenameFieldDispatcher dispatcher) {
        RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(project);
        // If checking for transitivity instead of conflicts
        if(dispatcher.isSimplify()) {
            this.isTransitive = cell.checkTransitivity(this.refactoringObject, dispatcherObject);
            // Update dispatcher refactoring
            dispatcher.setRefactoringObject(dispatcherObject);
        }
        // Otherwise check for conflicts
        else {
            this.isConflicting = cell.renameFieldRenameFieldConflictCell(this.refactoringObject, dispatcherObject);
            if(isConflicting) {
                dispatcherObject.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
            System.out.println("Rename Field/Rename Field conflict: " + isConflicting);
        }
    }

    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        RefactoringObject classObject = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameFieldMoveRenameClassCell.checkCombination(classObject, this.refactoringObject);
            dispatcher.setRefactoringObject(classObject);
        }
    }
}
