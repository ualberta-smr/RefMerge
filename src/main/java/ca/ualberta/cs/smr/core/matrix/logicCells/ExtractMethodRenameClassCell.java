package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;

public class ExtractMethodRenameClassCell {

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
}
