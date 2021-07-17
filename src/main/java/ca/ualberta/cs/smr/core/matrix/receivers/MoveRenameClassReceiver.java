package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import org.refactoringminer.api.RefactoringType;

public class MoveRenameClassReceiver extends Receiver {

    /*
     * Check if we can simplify rename class/rename method and update. If we are not checking for transitivity, check for
     * rename method/rename method dependence.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject methodRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            MoveRenameClassMoveRenameMethodCell.checkCombination(methodRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(methodRefactoring);
        }
        else {
            boolean isDependent = MoveRenameClassMoveRenameMethodCell.dependenceCell(methodRefactoring, this.refactoringObject);
            // If the rename method happens in the rename class, update the renamed method's class name
            if(isDependent) {
                methodRefactoring.setDestinationFilePath(refactoringObject.getDestinationFilePath());
                ((MoveRenameMethodObject) methodRefactoring).
                        setDestinationClassName(((MoveRenameClassObject) refactoringObject).getDestinationClassObject().getClassName());
            }
        }
    }

    /*
     * If simplify is true, check for rename class/rename class transitivity. If it is not null, check for
     * rename class/rename class conflict.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = MoveRenameClassMoveRenameClassCell
                    .checkTransitivity(this.refactoringObject, dispatcherRefactoring);
            dispatcher.setRefactoringObject(dispatcherRefactoring);

        }
        else {
            boolean isConflicting = MoveRenameClassMoveRenameClassCell
                    .conflictCell(dispatcherRefactoring, this.refactoringObject);
            if(isConflicting) {
                this.refactoringObject.setReplayFlag(false);
                dispatcherRefactoring.setReplayFlag(false);
                dispatcher.setRefactoringObject(dispatcherRefactoring);
                this.isConflicting = true;
            }
            else {
                boolean isDependent = MoveRenameClassMoveRenameClassCell
                        .checkDependence(dispatcherRefactoring, this.refactoringObject);
                if(isDependent) {
                    // If the dispatcher refactoring is the rename class refactoring
                    if(((MoveRenameClassObject) dispatcherRefactoring).isMoveMethod()) {
                        // Rename the class in the move class refactoring
                        ((MoveRenameClassObject) this.refactoringObject)
                                .getDestinationClassObject().updateClassName(((MoveRenameClassObject) dispatcherRefactoring)
                                .getDestinationClassObject().getClassName());
                        ((MoveRenameClassObject) this.refactoringObject).setType(RefactoringType.MOVE_RENAME_CLASS);
                        // Set the dispatcher refactoring to the updated move+rename receiver refactoring
                        ((MoveRenameClassObject) dispatcherRefactoring)
                                .setDestinationClassObject(((MoveRenameClassObject) this.refactoringObject).getDestinationClassObject());
                        ((MoveRenameClassObject) dispatcherRefactoring).setType(RefactoringType.MOVE_RENAME_CLASS);

                    }
                    // If the receiver refactoring is the rename class refactoring
                    else {
                        ((MoveRenameClassObject) dispatcherRefactoring)
                                .getDestinationClassObject().updateClassName(((MoveRenameClassObject) this.refactoringObject)
                                .getDestinationClassObject().getClassName());
                        ((MoveRenameClassObject) dispatcherRefactoring).setType(RefactoringType.MOVE_RENAME_CLASS);
                        ((MoveRenameClassObject) this.refactoringObject)
                                .setDestinationClassObject(((MoveRenameClassObject) dispatcherRefactoring).getDestinationClassObject());
                        ((MoveRenameClassObject) this.refactoringObject).setType(RefactoringType.MOVE_RENAME_CLASS);
                    }
                    dispatcherRefactoring.setReplayFlag(false);
                    dispatcher.setRefactoringObject(dispatcherRefactoring);
                }
            }
        }
    }

}
