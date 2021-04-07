package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

/*
 * Contains the logic check for rename class/rename method ordering dependence.
 */
public class RenameClassRenameMethodCell {

    /*
     *  Check if an ordering dependence exists between rename class and rename method refactorings.
     *  @param dispatcherNode: A node containing the dispatcher rename method refactoring.
     *  @param receiverNode: A node containing the receiver rename class refactoring.
     */
    public static boolean renameClassRenameMethodDependenceCell(Node dispatcherNode, Node receiverNode) {
        return checkRenameMethodRenameClassDependence(dispatcherNode, receiverNode);
    }

    public static boolean checkRenameMethodRenameClassDependence(Node methodNode, Node classNode) {
        Refactoring classRefactoring = classNode.getRefactoring();
        Refactoring methodRefactoring = methodNode.getRefactoring();
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }
}
