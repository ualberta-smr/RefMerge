package ca.ualberta.cs.smr.core.matrix.dependenceCheckers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class DependenceCheckers {

    public static boolean checkRenameMethodRenameClassDependence(Node classNode, Node methodNode) {
        Refactoring classRefactoring = classNode.getRefactoring();
        Refactoring methodRefactoring = methodNode.getRefactoring();
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }

    public static boolean checkRenameClassRenameClassDependence(Node firstNode, Node secondNode) {
        Refactoring firstRefactoring = firstNode.getRefactoring();
        Refactoring secondRefactoring = secondNode.getRefactoring();
        String firstClass = ((RenameClassRefactoring) firstRefactoring).getRenamedClassName();
        String secondClass = ((RenameClassRefactoring) secondRefactoring).getOriginalClassName();
        return firstClass.equals(secondClass);
    }
}
