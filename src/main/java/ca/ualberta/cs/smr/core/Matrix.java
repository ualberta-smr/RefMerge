package ca.ualberta.cs.smr.core;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

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
        System.out.println(rightRefactoring.toString());
        String originalLeftName = ((RenameOperationRefactoring) leftRefactoring).getOriginalOperation().getName();
        String originalRightName = ((RenameOperationRefactoring) rightRefactoring).getOriginalOperation().getName();
        String leftName = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation().getName();
        String rightName = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation().getName();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void checkRenameClass(Refactoring leftRefactoring, Refactoring rightRefactoring) {

    }

}
