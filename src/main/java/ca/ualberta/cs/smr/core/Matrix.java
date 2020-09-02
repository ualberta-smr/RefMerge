package ca.ualberta.cs.smr.core;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
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
                conflicting = checkRenameClass(leftRefactoring, rightRefactoring);
                System.out.println(conflicting);
                break;

        }
    }

    static boolean checkRenameMethod(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        if(methodRenamesConflict(leftRefactoring, rightRefactoring)) {
            return true;
        }

        return false;
    }

    static boolean methodRenamesConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        String originalLeftName = ((RenameOperationRefactoring) leftRefactoring).getOriginalOperation().getName();
        String originalRightName = ((RenameOperationRefactoring) rightRefactoring).getOriginalOperation().getName();
        String leftName = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation().getName();
        String rightName = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation().getName();
        String leftClass = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation().getClassName();
        String rightClass = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation().getClassName();

        // If the methods are in different classes, check if they override
        if(!leftClass.equals(rightClass)) {
            return methodInheritanceConflict(leftRefactoring, rightRefactoring);
        }
        // If the original method names are equal but the destination names are not equal
        else if(originalLeftName.equals(originalRightName) && !leftName.equals(rightName)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else if(!originalLeftName.equals(originalRightName) && leftName.equals(rightName)) {
            return true;
        }

        return false;
    }

    static boolean methodInheritanceConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        UMLOperation leftOriginalMethod = ((RenameOperationRefactoring) leftRefactoring).getOriginalOperation();
        UMLOperation rightOriginalMethod = ((RenameOperationRefactoring) rightRefactoring).getOriginalOperation();
        UMLOperation leftRefactoredMethod = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation();
        UMLOperation rightRefactoredMethod = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation();
        Class leftClass = leftOriginalMethod.getClass();
        Class rightClass = rightOriginalMethod.getClass();
        // If One of the classes extends the other
        if(leftClass.isAssignableFrom(rightClass) || rightClass.isAssignableFrom(leftClass)) {
            // If a method overrides the other
            if(leftOriginalMethod.getName().equals(rightOriginalMethod.getName())) {
                // And the refactored names are different, they no longer override and conflict
                if(!leftRefactoredMethod.getName().equals(rightRefactoredMethod.getName())) {
                    return true;
                }
            }
            else {
                // If a method overrides the other after refactoring, but they do not before, then it conflicts
                if(leftRefactoredMethod.getName().equals(rightRefactoredMethod.getName())) {
                    return true;
                }
            }
        }
        return false;


    }

    static boolean checkRenameClass(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        if(classRenamesConflict(leftRefactoring, rightRefactoring)) {
            return true;
        }
        return false;
    }

    static boolean classRenamesConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        String originalLeftClass = ((RenameClassRefactoring) leftRefactoring).getOriginalClassName();
        String originalRightClass = ((RenameClassRefactoring) rightRefactoring).getOriginalClassName();
        String renamedLeftClass = ((RenameClassRefactoring) leftRefactoring).getRenamedClassName();
        String renamedRightClass = ((RenameClassRefactoring) rightRefactoring).getRenamedClassName();
        String leftPackage = ((RenameClassRefactoring) leftRefactoring).getOriginalClass().getPackageName();
        String rightPackage = ((RenameClassRefactoring) rightRefactoring).getOriginalClass().getPackageName();

        // If the refactored classes are in different classes, they do not conflict
        if(!leftPackage.equals(rightPackage)) {
            return false;
        }
        // If the original class name is renamed to two separate names
        if(originalLeftClass.equals(originalRightClass) && !renamedLeftClass.equals(renamedRightClass)) {
            return true;
        }
        // If two classes are renamed to the same name
        else if(!originalLeftClass.equals(originalRightClass) && renamedLeftClass.equals(renamedRightClass)) {
            return true;
        }
        return false;
    }


}
