package ca.ualberta.cs.smr.core.matrix;

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
    static public void runMatrix(List<Refactoring> leftRefactorings, List<Refactoring> rightRefactorings) {
        // Iterates over the refactorings in the left commit
        for (Refactoring leftRefactoring : leftRefactorings) {
            // Compares the refactorings in the right commit against the left refactoring
            compareRefactorings(leftRefactoring, rightRefactorings);
        }
    }

    /*
     * This calls dispatch for each pair of refactorings to check for conflicts.
     */
    static void compareRefactorings(Refactoring leftRefactoring, List<Refactoring> rightRefactorings) {
        // Iterate over the right refactorings
        for(Refactoring rightRefactoring : rightRefactorings) {
            // Dispatch the refactoring elements to the correct conflict checker
            dispatch(leftRefactoring, rightRefactoring);
        }

    }

    /*
     * Perform double dispatch to check if the two refactoring elements conflict.
     */
    static void dispatch(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // Get the refactoring types so we can create the corresponding element and visitor
        String leftType = leftRefactoring.getName();
        String rightType = rightRefactoring.getName();
    }

    /*
     * Check if the rename class refactorings are conflicting
     */
    static boolean checkRenameClass(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // If they do conflict, return true. Otherwise return false
        return classRenamesConflict(leftRefactoring, rightRefactoring);
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
