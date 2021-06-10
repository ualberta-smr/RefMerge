package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodExtractMethodCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;

public class ExtractMethodReceiver extends Receiver {


    /*
     *  Check if we can simplify extract method/rename method and update or if there is conflict/dependence.
     */
    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject renameMethod = dispatcher.getRefactoringObject();
            // Need to use this.refactoringObject/dispatcher.refactoringObject or set dispatcher.refactoringObject
            this.isTransitive = ExtractMethodRenameMethodCell.checkExtractMethodRenameMethodTransitivity(renameMethod,
                    this.refactoringObject);
            dispatcher.setRefactoringObject(renameMethod);
        }
        else {
            RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
            ExtractMethodRenameMethodCell cell = new ExtractMethodRenameMethodCell(project);
            boolean isConflicting = cell.extractMethodRenameMethodConflictCell(dispatcherObject, this.refactoringObject);
            if(isConflicting) {
                System.out.println("Extract Method/Rename Method Conflict");
            }
            else {
                boolean isDependant = cell.extractMethodRenameMethodDependenceCell(dispatcherObject, this.refactoringObject);
                // If there is dependence, the source method of the extract method refactoring was renamed. Rename the source
                // method to represent this so we can replay the extract method properly
                if(isDependant) {
                    ((ExtractMethodObject) this.refactoringObject).
                            setOriginalMethodSignature(((RenameMethodObject) dispatcherObject).getDestinationMethodSignature());
                }
            }
        }
    }

    /*
     * Check for transitivity, conflict, and dependence in extract method/rename class and update.
     */
    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject renameClass = dispatcher.getRefactoringObject();
            ExtractMethodRenameClassCell.checkExtractMethodRenameClassCombination(renameClass, this.refactoringObject);
            dispatcher.setRefactoringObject(renameClass);
        }
        else {
            RefactoringObject dispatcherObject = dispatcher.getRefactoringObject();
            boolean isDependent = ExtractMethodRenameClassCell
                    .extractMethodRenameClassDependenceCell(dispatcherObject, this.refactoringObject);
            if(isDependent) {
                this.refactoringObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
                ((ExtractMethodObject) this.refactoringObject)
                        .setDestinationClassName(((RenameClassObject) dispatcherObject).getDestinationClassName());
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
