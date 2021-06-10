package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;

/*
 * Contains the logic check for extract method/rename class ordering dependence.
 */
public class ExtractMethodRenameClassCell {

    /*
     *  Check if an ordering dependence exists between extract method/rename class refactorings.
     */
    public static boolean extractMethodRenameClassDependenceCell(RefactoringObject renameClass, RefactoringObject extractMethod) {
        return checkExtractMethodRenameClassDependence(renameClass, extractMethod);
    }

    /*
     * Check if the rename class and the extract method refactorings are related
     */
    public static boolean checkExtractMethodRenameClassDependence(RefactoringObject renameClass, RefactoringObject extractMethod) {
        RenameClassObject renameClassObject = (RenameClassObject) renameClass;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String renameOriginalClassName = renameClassObject.getOriginalClassName();
        String extractOriginalClassname = extractMethodObject.getOriginalClassName();
        String extractDestinationClassname = extractMethodObject.getDestinationClassName();

        return extractOriginalClassname.equals(renameOriginalClassName)
                || extractDestinationClassname.equals(renameOriginalClassName);
    }

    /*
     * Check if the extract method and rename class refactorings can be combined. If there is a combination, update
     * the extract method refactoring.
     */
    public static void checkExtractMethodRenameClassCombination(RefactoringObject renameClass,
                                                                RefactoringObject extractMethod) {
        RenameClassObject renameClassObject = (RenameClassObject) renameClass;
        String originalClassClass = renameClassObject.getOriginalClassName();
        String destinationClassClass = renameClassObject.getDestinationClassName();
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String sourceMethodClass = extractMethodObject.getOriginalClassName();
        String extractedMethodClass = extractMethodObject.getDestinationClassName();

        // If the extracted method's class has the original class name, then the extract method happened before the rename
        // class. update the extracted method's class name to the renamed class name
        if(extractedMethodClass.equals(originalClassClass)) {
            extractMethod.setDestinationFilePath(renameClassObject.getDestinationFilePath());
            ((ExtractMethodObject) extractMethod).setDestinationClassName(destinationClassClass);
        }
        // If the source method's class hsa the destination class name, then the extract method happened in a commit after
        // the rename class. Use the original class name for the source method for conflict and dependence checks.
        else if(sourceMethodClass.equals(destinationClassClass)) {
            extractMethod.setOriginalFilePath(renameClassObject.getOriginalFilePath());
            ((ExtractMethodObject) extractMethod).setOriginalClassName(originalClassClass);
        }

    }
}
