package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class MoveRenameMethodReceiver extends Receiver {

    /*
     * If simplify is true, check for rename method/rename method transitivity. If it is not null, check for
     * rename method/rename method conflict.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        // Check for rename method/rename method transitivity
        if(dispatcher.isSimplify()) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
            this.isTransitive = cell.checkMoveRenameMethodMoveRenameMethodTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for rename method/rename method conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
            boolean isConflicting = cell.moveRenameMethodMoveRenameMethodConflictCell(dispatcherRefactoring, this.refactoringObject);
            System.out.println("Rename Method/Rename Method conflict: " + isConflicting);
            // They cannot be both conflicting and dependent
            if(!isConflicting) {
                boolean isDependent = cell.checkMoveRenameMethodMoveRenameMethodDependence(dispatcherRefactoring, this.refactoringObject);
                if(isDependent) {
                    // If the dispatcher refactoring is the rename method operation
                    if(((MoveRenameMethodObject) dispatcherRefactoring).isRenameMethod()) {
                        // Set the renamed method's class and file to the moved method's class and file
                        dispatcherRefactoring.setDestinationFilePath(this.refactoringObject.getDestinationFilePath());
                        ((MoveRenameMethodObject) dispatcherRefactoring)
                                .setDestinationClassName(((MoveRenameMethodObject) this.refactoringObject).getDestinationClassName());
                        ((MoveRenameMethodObject) this.refactoringObject)
                                .setDestinationMethodSignature(((MoveRenameMethodObject) dispatcherRefactoring)
                                        .getDestinationMethodSignature());
                        dispatcherRefactoring.setReplayFlag(false);
                        dispatcher.setRefactoringObject(dispatcherRefactoring);
                    }
                    // Otherwise, if the receiver refactoring is the rename method operation
                    else {
                        this.refactoringObject.setDestinationFilePath(dispatcherRefactoring.getDestinationFilePath());
                        ((MoveRenameMethodObject) this.refactoringObject)
                                .setDestinationClassName(((MoveRenameMethodObject) dispatcherRefactoring).getDestinationClassName());
                        // Set the moved method's signature to the renamed method's signature
                        ((MoveRenameMethodObject) dispatcherRefactoring)
                                .setDestinationMethodSignature(((MoveRenameMethodObject) this.refactoringObject)
                                        .getDestinationMethodSignature());
                        dispatcherRefactoring.setReplayFlag(false);
                        dispatcher.setRefactoringObject(dispatcherRefactoring);
                    }
                }
            }
        }
    }

}
