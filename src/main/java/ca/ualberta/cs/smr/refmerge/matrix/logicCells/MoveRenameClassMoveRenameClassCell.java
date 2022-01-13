package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;


/*
 * Contains the logic check for move+rename class/move+rename class refactoring conflict.
 */
public class MoveRenameClassMoveRenameClassCell {

    /*
     *  Check if a conflict exists between move+rename class/move+rename class refactorings. The conflict that can exist is a
     *  naming conflict.
     */
    public static boolean conflictCell(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        if(checkConflict(firstRefactoring, secondRefactoring)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    /*
     * If two classes are moved or renamed to the same class or one class is refactored to two different destinations, then there is a
     * class naming conflict.
     */
    public static boolean checkConflict(RefactoringObject dispatcherRefactoringObject, RefactoringObject receiverRefactoringObject) {
        MoveRenameClassObject dispatcherClassRefactoring = (MoveRenameClassObject) dispatcherRefactoringObject;
        MoveRenameClassObject receiverClassRefactoring = (MoveRenameClassObject) receiverRefactoringObject;
        ClassObject dispatcherOriginalClass = dispatcherClassRefactoring.getOriginalClassObject();
        ClassObject receiverOriginalClass = receiverClassRefactoring.getOriginalClassObject();
        ClassObject dispatcherDestinationClass = dispatcherClassRefactoring.getDestinationClassObject();
        ClassObject receiverDestinationClass = receiverClassRefactoring.getDestinationClassObject();

        // If the dispatcher and the receiver are both rename, move, or rename+move refactorings
        if((dispatcherClassRefactoring.isRenameMethod() && receiverClassRefactoring.isRenameMethod())
                || (dispatcherClassRefactoring.isMoveMethod() && receiverClassRefactoring.isMoveMethod())) {
            // If the original class is refactored to two different destinations
            if(dispatcherOriginalClass.equalsClass(receiverOriginalClass)
                    && dispatcherClassRefactoring.getOriginalFilePath().equals(receiverClassRefactoring.getOriginalFilePath())
                    && !dispatcherDestinationClass.equalsClass(receiverDestinationClass)) {
                return true;
            }
            // If two classes are refactored to the same class
            if(dispatcherClassRefactoring.getDestinationFilePath().equals(receiverClassRefactoring.getDestinationFilePath())) {
            return !dispatcherOriginalClass.equalsClass(receiverOriginalClass)
                        && dispatcherDestinationClass.equalsClass(receiverDestinationClass);
            }
        }
        return false;
    }

    /*
     * Check for dependence between move+rename class refactorings where one is move method and the other is rename method.
     */
    public static boolean checkDependence(RefactoringObject dispatcherRefactoringObject, RefactoringObject receiverRefactoringObject) {
        MoveRenameClassObject dispatcherClassRefactoring = (MoveRenameClassObject) dispatcherRefactoringObject;
        MoveRenameClassObject receiverClassRefactoring = (MoveRenameClassObject) receiverRefactoringObject;

        String dispatcherOriginalClassName = dispatcherClassRefactoring.getOriginalClassObject().getClassName();
        String dispatcherOriginalPackageName = dispatcherClassRefactoring.getOriginalClassObject().getPackageName();
        String receiverOriginalClassName = receiverClassRefactoring.getOriginalClassObject().getClassName();
        String receiverOriginalPackageName = receiverClassRefactoring.getOriginalClassObject().getPackageName();

        // If the original classes are not the same, there is no dependence between the class refactorings
        if(!(dispatcherOriginalClassName.equals(receiverOriginalClassName) && dispatcherOriginalPackageName.equals(receiverOriginalPackageName))) {
            return false;
        }

        // If both class refactorings are move class, then there cannot be dependence
        if(dispatcherClassRefactoring.isMoveMethod() && receiverClassRefactoring.isMoveMethod()) {
            return false;
        }

        // If both class refactorings are rename class, then there cannot be dependence
        return !(dispatcherClassRefactoring.isRenameMethod() && receiverClassRefactoring.isRenameMethod());
    }

    /*
     * Checks for transitivity between the first and second move+rename class refactorings. If there is transitivity, the
     * first move+rename class refactoring is updated.
     */
    public static boolean checkTransitivity(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        boolean isTransitive = false;
        MoveRenameClassObject firstObject = (MoveRenameClassObject) firstRefactoring;
        MoveRenameClassObject secondObject = (MoveRenameClassObject) secondRefactoring;

        String firstDestinationPackage = firstObject.getDestinationClassObject().getPackageName();
        String firstDestinationClass = firstObject.getDestinationClassObject().getClassName();
        String secondOriginalPackage = secondObject.getOriginalClassObject().getPackageName();
        String secondOriginalClass = secondObject.getOriginalClassObject().getClassName();

        // If the refactored class of the first refactoring is the original class of the second refactoring
        if(firstDestinationClass.equals(secondOriginalClass) && firstDestinationPackage.equals(secondOriginalPackage)) {
            isTransitive = true;
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((MoveRenameClassObject) firstRefactoring).setDestinationClassObject(secondObject.getDestinationClassObject());
        }

        return isTransitive;
    }
}
