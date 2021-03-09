package ca.ualberta.cs.smr.core.matrix.dependenceCheckers;

import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class DependenceCheckers {

    public static  boolean checkRenameMethodRenameClassDependence(Refactoring classRefactoring, Refactoring methodRefactoring) {
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }
}
