package ca.ualberta.cs.smr.core;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

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
        RefactoringType type = leftRefactoring.getRefactoringType();
        switch(type) {
            case RENAME_METHOD:
                checkRenameMethod(leftRefactoring, rightRefactoring);
                break;

        }
    }

    static void checkRenameMethod(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        
    }


}
