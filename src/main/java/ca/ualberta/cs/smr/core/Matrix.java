package ca.ualberta.cs.smr.core;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import java.util.List;

/*
 * The logic and dispatching for the conflict matrix happens in Matrix.
 */

public class Matrix {

    /*
     * Iterate through each of the left refactorings to compare against the right refactorings.
     */
    static void runMatrix(List<Refactoring> leftRefactorings, List<Refactoring> rightRefactorings) {
        // Iterates over the refactorings in the left commit
        for (Refactoring leftRefactoring : leftRefactorings) {
            // Compares the refactorings in the right commit against the left refactoring
            compareRefactorings(leftRefactoring, rightRefactorings);
        }
    }

    /*
     * This compares each refactoring in the right commit to see if the conflict with the left refactoring
     */
    static void compareRefactorings(Refactoring leftRefactoring, List<Refactoring> rightRefactorings) {
        // Get the type of the left refactoring
        RefactoringType leftType = leftRefactoring.getRefactoringType();
        // Iterate over the right refactorings
        for(Refactoring rightRefactoring : rightRefactorings) {
            // Get the type of the right refactoring
            RefactoringType rightType = rightRefactoring.getRefactoringType();
            // Compare to see if the types of refactorings are the same
            if(leftType == rightType) {
                // If they're the same type, see if they are conflicting
                checkIfConflicting(leftRefactoring, rightRefactoring);
            }
        }

    }

    /*
     * Check if a pair of refactorings conflict.
     */
    static void checkIfConflicting(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        boolean conflicting = false;
        RefactoringType type = leftRefactoring.getRefactoringType();
        switch(type) {
            case RENAME_METHOD:
                // Check if the rename method refactorings are conflicting
                conflicting = checkRenameMethod(leftRefactoring, rightRefactoring);
                // Print if they're conflicting or not for debugging
                System.out.println(conflicting);
                break;
            case RENAME_CLASS:
                // Check if the rename class refactorings are conflicting
                conflicting = checkRenameClass(leftRefactoring, rightRefactoring);
                // Print if they're conflicting or not for debugging
                System.out.println(conflicting);
                break;

        }
    }

    static boolean checkRenameMethod(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // If the method renames conflict
        if(methodRenamesConflict(leftRefactoring, rightRefactoring)) {
            return true;
        }

        return false;
    }

    /*
     * Check each similar entity and inheritance case to see if the method renames conflict.
     */
    static boolean methodRenamesConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // Get the UMLOperation for each refactoring
        UMLOperation leftOperation = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation();
        UMLOperation rightOperation = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation();
        // Get the name of the methods before they are refactored
        String originalLeftName = leftOperation.getName();
        String originalRightName = rightOperation.getName();
        // Get the name of the methods after they are refactored
        String leftName = leftOperation.getName();
        String rightName = rightOperation.getName();
        // Get the names of the classes that the methods are in
        String leftClass = leftOperation.getClassName();
        String rightClass = rightOperation.getClassName();

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

    /*
     * Check each of the inheritance cases to see if the method renames conflict
     */
    static boolean methodInheritanceConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // Get the name of the methods before they are renamed
        UMLOperation leftOriginalMethod = ((RenameOperationRefactoring) leftRefactoring).getOriginalOperation();
        UMLOperation rightOriginalMethod = ((RenameOperationRefactoring) rightRefactoring).getOriginalOperation();
        // get the name of the methods after they are renamed
        UMLOperation leftRefactoredMethod = ((RenameOperationRefactoring) leftRefactoring).getRenamedOperation();
        UMLOperation rightRefactoredMethod = ((RenameOperationRefactoring) rightRefactoring).getRenamedOperation();
        // Get the classes of the methods
        Class leftClass = leftOriginalMethod.getClass();
        Class rightClass = rightOriginalMethod.getClass();
        // If one of the classes extends the other
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

    /*
     * Check if the rename class refactorings are conflicting
     */
    static boolean checkRenameClass(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // If they do conflict, return true. Otherwise return false
        if(classRenamesConflict(leftRefactoring, rightRefactoring)) {
            return true;
        }
        return false;
    }

    /*
     * Check each of the similar entity and inheritance cases to see if they are conflicting
     */
    static boolean classRenamesConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // get the original class names
        String originalLeftClass = ((RenameClassRefactoring) leftRefactoring).getOriginalClassName();
        String originalRightClass = ((RenameClassRefactoring) rightRefactoring).getOriginalClassName();
        // get the refactored class names
        String renamedLeftClass = ((RenameClassRefactoring) leftRefactoring).getRenamedClassName();
        String renamedRightClass = ((RenameClassRefactoring) rightRefactoring).getRenamedClassName();
        // get the package of each class
        String leftPackage = ((RenameClassRefactoring) leftRefactoring).getOriginalClass().getPackageName();
        String rightPackage = ((RenameClassRefactoring) rightRefactoring).getOriginalClass().getPackageName();

        // If the refactored classes are in different classes, check if inheritance conflict occurs
        if(!leftPackage.equals(rightPackage)) {
            return classInheritanceConflict(leftRefactoring, rightRefactoring);
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

    /*
     * Check if there is a class inheritance conflict
     */
    static boolean classInheritanceConflict(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // Get the original class names
        UMLClass leftOriginalClass = ((RenameClassRefactoring) leftRefactoring).getOriginalClass();
        UMLClass rightOriginalClass = ((RenameClassRefactoring) rightRefactoring).getOriginalClass();
        // get the refactored class names
        UMLClass leftRenamedClass = ((RenameClassRefactoring) leftRefactoring).getRenamedClass();
        UMLClass rightRenamedClass = ((RenameClassRefactoring) rightRefactoring).getRenamedClass();
        // Get the original classes to see if they are super/subclasses
        Class leftOriginal = leftOriginalClass.getClass();
        Class rightOriginal = rightOriginalClass.getClass();
        // get the refactored classes to see if they are super/subclasses
        Class leftRenamed = leftRenamedClass.getClass();
        Class rightRenamed = rightRenamedClass.getClass();


        // If there is inheritance
        if(leftOriginal.isAssignableFrom(rightOriginal) || rightOriginal.isAssignableFrom(leftOriginal)) {
            // If there is not inheritance after the renaming and the names are different, then there was an inheritance conflict
            if(!leftRenamed.isAssignableFrom(rightRenamed) && !rightRenamed.isAssignableFrom(leftRenamed)) {
                return true;
            }
        }
        // If there is no inheritance
        else {
            // Accidental inheritance conflict
            if(leftRenamed.isAssignableFrom(rightRenamed) || rightRenamed.isAssignableFrom(leftRenamed)) {
                return true;
            }
        }
        return false;
    }


}
