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



}
