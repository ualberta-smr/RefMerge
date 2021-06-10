package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;

/*
 * Contains the logic check for rename class/rename class refactoring conflict.
 */
public class RenameClassRenameClassCell {

    /*
     *  Check if a conflict exists between rename class/rename class refactorings. The conflict that can exist is a
     *  naming conflict.
     */
    public static boolean renameClassRenameClassConflictCell(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        if(checkClassNamingConflict(firstRefactoring, secondRefactoring)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    /*
     * If two classes are renamed to the same class or one class is renamed to two different names, then there is a
     * class naming conflict.
     */
    public static boolean checkClassNamingConflict(RefactoringObject dispatcherRefactoringObject,
                                                   RefactoringObject receiverRefactoringObject) {
        RenameClassObject dispatcherRenameClass = (RenameClassObject) dispatcherRefactoringObject;
        RenameClassObject receiverRenameClass = (RenameClassObject) receiverRefactoringObject;
        String dispatcherOriginalClassName = dispatcherRenameClass.getOriginalClassName();
        String receiverOriginalClassName = receiverRenameClass.getOriginalClassName();
        String dispatcherDestinationClassName = dispatcherRenameClass.getDestinationClassName();
        String receiverDestinationClassName = receiverRenameClass.getDestinationClassName();

        return checkNamingConflict(dispatcherOriginalClassName, receiverOriginalClassName,
                dispatcherDestinationClassName, receiverDestinationClassName);
    }

    /*
     * Checks for transitivity between the first and second rename class refactorings. If there is transitivity, the
     * first rename class refactoring is updated.
     */
    public static boolean checkRenameClassRenameClassTransitivity(RefactoringObject firstRefactoring,
                                                                  RefactoringObject secondRefactoring) {
        boolean isTransitive = false;
        RenameClassObject firstObject = (RenameClassObject) firstRefactoring;
        RenameClassObject secondObject = (RenameClassObject) secondRefactoring;
        String firstDestinationClass = firstObject.getDestinationClassName();
        String secondOriginalClass = secondObject.getOriginalClassName();

        // If the renamed class of the first refactoring is the original class of the second refactoring
        if(firstDestinationClass.equals(secondOriginalClass)) {
            isTransitive = true;
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((RenameClassObject) firstRefactoring).setDestinationClassName(secondObject.getDestinationClassName());
        }

        return isTransitive;
    }
}
