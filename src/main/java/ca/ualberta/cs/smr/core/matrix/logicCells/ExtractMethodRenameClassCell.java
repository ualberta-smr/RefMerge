package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;

/*
 * Contains the logic check for extract method/rename class ordering dependence.
 */
public class ExtractMethodRenameClassCell {

    /*
     *  Check if an ordering dependence exists between extract method/rename class refactorings.
     *  @param dispatcherNode: A node containing the dispatcher rename class refactoring.
     *  @param receiverNode: A node containing the receiver extract method refactoring.
     */
    public static boolean extractMethodRenameClassDependenceCell(Node dispatcherNode, Node receiverNode) {
        return checkExtractMethodRenameClassDependence(dispatcherNode, receiverNode);
    }

    public static boolean checkExtractMethodRenameClassDependence(Node dispatcherNode, Node receiverNode) {
        RenameClassRefactoring renameClassRefactoring = (RenameClassRefactoring) dispatcherNode.getRefactoring();
        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) receiverNode.getRefactoring();
        String originalClassName = renameClassRefactoring.getOriginalClassName();
        String sourceMethodClassName = extractOperationRefactoring.getSourceOperationBeforeExtraction().getClassName();
        String extractMethodClassName = extractOperationRefactoring.getExtractedOperation().getClassName();

        return sourceMethodClassName.equals(originalClassName) || extractMethodClassName.equals(originalClassName);
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
