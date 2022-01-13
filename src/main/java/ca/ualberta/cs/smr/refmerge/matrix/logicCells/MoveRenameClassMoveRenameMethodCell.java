package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;

/*
 * Contains the logic check for move+rename class/move+rename method ordering dependence.
 */
public class MoveRenameClassMoveRenameMethodCell {

    /*
     *  Check if an ordering dependence exists between move+rename class and rename method refactorings.
     */
    public static boolean dependenceCell(RefactoringObject methodObject, RefactoringObject classObject) {
        return checkDependence(methodObject, classObject);
    }

    /*
     * Check if the move+rename class needs to be performed before the rename method.
     */
    public static boolean checkDependence(RefactoringObject methodRefactoringObject, RefactoringObject classRefactoringObject) {
        String classOriginalClassName = ((MoveRenameClassObject) classRefactoringObject).getOriginalClassObject().getClassName();
        String methodOriginalClassName = ((MoveRenameMethodObject) methodRefactoringObject).getOriginalClassName();
        return classOriginalClassName.equals(methodOriginalClassName);
    }

    /*
     * Check if the move+rename class and move+rename method refactorings can be simplified. If they can, update the move+rename method
     * refactoring class details.
     */
    public static void checkCombination(RefactoringObject renameMethod, RefactoringObject renameClass) {
        MoveRenameMethodObject methodObject = (MoveRenameMethodObject) renameMethod;
        String originalMethodClass = methodObject.getOriginalClassName();
        String destinationMethodClass = methodObject.getDestinationClassName();
        MoveRenameClassObject classObject = (MoveRenameClassObject) renameClass;
        String originalClassClass = classObject.getOriginalClassObject().getClassName();
        String destinationClassClass = classObject.getDestinationClassObject().getClassName();

        // If the original class of the method refactoring is the same as the original class of the class
        // refactoring, update the destination class for the method refactoring to be the destination class of
        // the class refactoring
        if (originalMethodClass.equals(originalClassClass) && !destinationMethodClass.equals(destinationClassClass)) {
            renameMethod.setDestinationFilePath(classObject.getDestinationFilePath());
            ((MoveRenameMethodObject) renameMethod).setDestinationClassName(classObject.getDestinationClassObject().getClassName());
        }
        // If the destination classes for the rename method and rename class refactorings are the same but the original
        // names are different, then the class was renamed before the method was and we need to update the original
        // class name for the rename method refactoring
        else if (!originalMethodClass.equals(originalClassClass) && destinationMethodClass.equals(destinationClassClass)) {
            renameMethod.setOriginalFilePath(classObject.getOriginalFilePath());
            ((MoveRenameMethodObject) renameMethod).setOriginalClassName(classObject.getOriginalClassObject().getClassName());
        }
        // If the destination class of the rename method is equal to the destination class of the rename class, then the
        // destination of the rename method class needs to be updated to the rename class's destination class name.
        else if (destinationMethodClass.equals(originalClassClass) && !originalMethodClass.equals(originalClassClass)) {
            renameMethod.setDestinationFilePath(classObject.getDestinationFilePath());
            ((MoveRenameMethodObject) renameMethod).setDestinationClassName(classObject.getDestinationClassObject().getClassName());
        }
    }
}
