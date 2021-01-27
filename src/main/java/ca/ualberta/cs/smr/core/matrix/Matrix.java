package ca.ualberta.cs.smr.core.matrix;


import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.HashMap;
import java.util.List;

/*
 * The logic and dispatching for the conflict matrix happens in Matrix.
 */

public class Matrix {

    static final HashMap<RefactoringType, RefactoringElement> elementMap = new HashMap<RefactoringType, RefactoringElement>() {{
       put(RefactoringType.RENAME_METHOD, new RenameMethodElement());
       put(RefactoringType.RENAME_CLASS, new RenameClassElement());
    }};


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
        RefactoringType leftType = leftRefactoring.getRefactoringType();
        RefactoringType rightType = rightRefactoring.getRefactoringType();
        RefactoringElement element = makeElement(leftType, leftRefactoring);
    }

    static private RefactoringElement makeElement(RefactoringType type, Refactoring ref) {
        RefactoringElement element = elementMap.get(type);
        element.set(ref);
        return element;
    }


}
