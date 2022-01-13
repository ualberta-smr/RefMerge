package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;

/*
 * Contains the logic check for extract method/move+rename class ordering dependence.
 */
public class ExtractMethodMoveRenameClassCell {

    /*
     *  Check if an ordering dependence exists between extract method/move+rename class refactorings.
     */
    public static boolean dependenceCell(RefactoringObject renameClass, RefactoringObject extractMethod) {
        return checkDependence(renameClass, extractMethod);
    }

    /*
     * Check if the move+rename class and the extract method refactorings are related
     */
    public static boolean checkDependence(RefactoringObject renameClass, RefactoringObject extractMethod) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) renameClass;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String renameOriginalClassName = moveRenameClassObject.getOriginalClassObject().getClassName();
        String extractOriginalClassname = extractMethodObject.getOriginalClassName();
        String extractDestinationClassname = extractMethodObject.getDestinationClassName();

        return extractOriginalClassname.equals(renameOriginalClassName)
                || extractDestinationClassname.equals(renameOriginalClassName);
    }

    /*
     * Check if the extract method and  class refactorings can be combined. If there is a combination, update
     * the extract method refactoring.
     */
    public static void checkCombination(RefactoringObject renameClass, RefactoringObject extractMethod) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) renameClass;
        String originalClassClass = moveRenameClassObject.getOriginalClassObject().getClassName();
        String destinationClassClass = moveRenameClassObject.getDestinationClassObject().getClassName();
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String sourceMethodClass = extractMethodObject.getOriginalClassName();
        String extractedMethodClass = extractMethodObject.getDestinationClassName();

        // If the extracted method's class has the original class name, then the extract method happened before the rename
        // class. update the extracted method's class name to the renamed class name
        if(extractedMethodClass.equals(originalClassClass)) {
            extractMethod.setDestinationFilePath(moveRenameClassObject.getDestinationFilePath());
            ((ExtractMethodObject) extractMethod).setDestinationClassName(destinationClassClass);
        }
        // If the source method's class hsa the destination class name, then the extract method happened in a commit after
        // the rename class. Use the original class name for the source method for conflict and dependence checks.
        else if(sourceMethodClass.equals(destinationClassClass)) {
            extractMethod.setOriginalFilePath(moveRenameClassObject.getOriginalFilePath());
            ((ExtractMethodObject) extractMethod).setOriginalClassName(originalClassClass);
        }

    }
}
