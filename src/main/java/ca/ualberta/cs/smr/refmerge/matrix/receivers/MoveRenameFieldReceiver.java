package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.*;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.MoveRenameFieldMoveRenameClassCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.MoveRenameFieldMoveRenameFieldCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;


public class MoveRenameFieldReceiver extends Receiver {

    @Override
    public void receive(MoveRenameFieldDispatcher dispatcher) {
        RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(project);
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
                System.out.println("Rename Field/Rename Field conflict: " + isConflicting);
            }
            if(!isConflicting) {
                boolean isDependent = cell.checkDependence(dispatcherObject, this.refactoringObject);
                if(isDependent) {
                    // If the dispatcher refactoring is the rename method operation
                    if(((MoveRenameFieldObject) dispatcherObject).isRename()) {
                        // Set the renamed field's class and file to the moved field's class and file
                        dispatcherObject.setDestinationFilePath(this.refactoringObject.getDestinationFilePath());
                        ((MoveRenameFieldObject) dispatcherObject)
                                .setDestinationClassName(((MoveRenameFieldObject) this.refactoringObject).getDestinationClass());
                        ((MoveRenameFieldObject) dispatcherObject)
                                .setDestinationFieldName(((MoveRenameFieldObject) this.refactoringObject).getDestinationName());
                    }
                    // Otherwise, if the receiver refactoring is the rename field operation
                    else {
                        this.refactoringObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
                        ((MoveRenameFieldObject) this.refactoringObject)
                                .setDestinationClassName(((MoveRenameFieldObject) dispatcherObject).getDestinationClass());
                        // Set the moved method's signature to the renamed method's signature
                        ((MoveRenameFieldObject) dispatcherObject)
                                .setDestinationFieldName(((MoveRenameFieldObject) this.refactoringObject)
                                        .getDestinationName());
                    }
                    dispatcherObject.setReplayFlag(false);
                    dispatcher.setRefactoringObject(dispatcherObject);
                }
            }
        }
    }

    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        RefactoringObject classObject = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            MoveRenameFieldMoveRenameClassCell.checkCombination(classObject, this.refactoringObject);
            dispatcher.setRefactoringObject(classObject);
        }
    }
}
