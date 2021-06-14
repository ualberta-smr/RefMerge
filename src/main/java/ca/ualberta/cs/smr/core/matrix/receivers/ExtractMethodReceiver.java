package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodExtractMethodCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodMoveRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;

public class ExtractMethodReceiver extends Receiver {


    /*
     *  Check if we can simplify extract method/rename method and update or if there is conflict/dependence.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject renameMethod = dispatcher.getRefactoringObject();
            // Need to use this.refactoringObject/dispatcher.refactoringObject or set dispatcher.refactoringObject
            this.isTransitive = ExtractMethodMoveRenameMethodCell.checkExtractMethodRenameMethodTransitivity(renameMethod,
                    this.refactoringObject);
            dispatcher.setRefactoringObject(renameMethod);
        }
        else {
            RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
            ExtractMethodMoveRenameMethodCell cell = new ExtractMethodMoveRenameMethodCell(project);
            boolean isConflicting = cell.extractMethodRenameMethodConflictCell(dispatcherObject, this.refactoringObject);
            if(isConflicting) {
                System.out.println("Extract Method/Rename Method Conflict");
            }
            else {
                boolean isDependent = cell.extractMethodRenameMethodDependenceCell(dispatcherObject, this.refactoringObject);
                // If there is dependence, the source method of the extract method refactoring was renamed. Rename the source
                // method to represent this so we can replay the extract method properly
                if(isDependent) {
                    ((ExtractMethodObject) this.refactoringObject).
                            setOriginalMethodSignature(((MoveRenameMethodObject) dispatcherObject).getDestinationMethodSignature());
                    dispatcherObject.setReplayFlag(false);
                    dispatcher.setRefactoringObject(dispatcherObject);
                }
            }
        }
    }

    /*
     * Check for transitivity, conflict, and dependence in extract method/rename class and update.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject renameClass = dispatcher.getRefactoringObject();
            ExtractMethodMoveRenameClassCell.checkExtractMethodMoveRenameClassCombination(renameClass, this.refactoringObject);
            dispatcher.setRefactoringObject(renameClass);
        }
        else {
            RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
            boolean isDependent = ExtractMethodMoveRenameClassCell
                    .extractMethodMoveRenameClassDependenceCell(dispatcherObject, this.refactoringObject);
            if(isDependent) {
                this.refactoringObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
                ((ExtractMethodObject) this.refactoringObject)
                        .setDestinationClassName(((MoveRenameClassObject) dispatcherObject).getDestinationClassObject().getClassName());
            }
        }
    }

    /*
     * Check for extract method/extract method conflict.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
        boolean isConflicting = cell.extractMethodExtractMethodConflictCell(dispatcherObject, this.refactoringObject);
        if(isConflicting) {
            System.out.println("Extract Method/Extract Method Conflict");
        }
    }
}
