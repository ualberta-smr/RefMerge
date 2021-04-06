package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class RenameClassRenameMethodCell {

    public static boolean renameClassRenameMethodDependenceCell(Node elementNode, Node visitorNode) {
        return checkRenameMethodRenameClassDependence(elementNode, visitorNode);
    }

    public static boolean checkRenameMethodRenameClassDependence(Node classNode, Node methodNode) {
        Refactoring classRefactoring = classNode.getRefactoring();
        Refactoring methodRefactoring = methodNode.getRefactoring();
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }
}
