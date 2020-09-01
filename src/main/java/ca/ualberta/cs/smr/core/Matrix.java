package ca.ualberta.cs.smr.core;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Matrix {

    static void runMatrix(List<Refactoring> leftRefactorings, List<Refactoring> rightRefactorings) {
        for (Refactoring leftRefactoring : leftRefactorings) {
            compareRefactorings(leftRefactoring, rightRefactorings);
        }
    }

    static void compareRefactorings(Refactoring leftRefactoring, List<Refactoring> rightRefactorings) {
        RefactoringType leftType = leftRefactoring.getRefactoringType();
        for(Refactoring rightRefactoring : rightRefactorings) {
            RefactoringType rightType = rightRefactoring.getRefactoringType();
            if(leftType == rightType) {
                checkIfConflicting(leftRefactoring, rightRefactoring);
            }
        }

    }

    static void checkIfConflicting(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        boolean conflicting = false;
        RefactoringType type = leftRefactoring.getRefactoringType();
        switch(type) {
            case RENAME_METHOD:
                conflicting = checkRenameMethod(leftRefactoring, rightRefactoring);
                System.out.println(conflicting);
                break;
            case RENAME_CLASS:
                checkRenameClass(leftRefactoring, rightRefactoring);
                break;

        }
    }

    static boolean checkRenameMethod(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        if(renamesConflict(leftRefactoring, rightRefactoring)) {
            return true;
        }

        return false;
    }

    static boolean renamesConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        String originalLeftName = ((RenameOperationRefactoring) leftRefactoring).getOriginalOperation().getName();
        String originalRightName = ((RenameOperationRefactoring) rightRefactoring).getOriginalOperation().getName();
        String leftName = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation().getName();
        String rightName = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation().getName();
        String leftClass = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation().getClassName();
        String rightClass = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation().getClassName();

        // If the methods are in different classes, they do not conflict
        if(!leftClass.equals(rightClass)) {
            return false;
        }
        // If the original method names are equal but the destination names are not equal
        else if(originalLeftName.equals(originalRightName) && !leftName.equals(rightName)) {
            return true;
        }


        return false;
    }

    static void checkRenameClass(Refactoring leftRefactoring, Refactoring rightRefactoring) {

    }

}
