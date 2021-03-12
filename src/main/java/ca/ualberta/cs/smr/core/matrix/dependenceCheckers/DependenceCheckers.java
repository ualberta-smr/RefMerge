package ca.ualberta.cs.smr.core.matrix.dependenceCheckers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class DependenceCheckers {

    public static boolean checkRenameMethodRenameClassDependence(Node elementNode, Node visitorNode) {
        Refactoring classRefactoring = elementNode.getRefactoring();
        Refactoring methodRefactoring = visitorNode.getRefactoring();
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }
}
