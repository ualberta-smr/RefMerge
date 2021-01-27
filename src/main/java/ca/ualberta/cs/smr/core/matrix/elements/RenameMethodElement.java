package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

/*
 * Checks if visitor refactorings confict with a rename method refactoring.
 */
public class RenameMethodElement extends RefactoringElement {
    Refactoring elementRef;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Refactoring ref) {
        elementRef = ref;
    }

    /*
     *  Check if a rename method refactoring conflicts with a second rename method refactoring.
     */
    public boolean checkRenameMethodConflict(Refactoring visitorRef) {
        // Get the original and renamed UMLOperation for each refactoring
        UMLOperation leftOriginalOperation = ((RenameOperationRefactoring) elementRef).getOriginalOperation();
        UMLOperation rightOriginalOperation = ((RenameOperationRefactoring) visitorRef).getOriginalOperation();
        UMLOperation leftOperation = ((RenameOperationRefactoring) elementRef).getRenamedOperation();
        UMLOperation rightOperation = ((RenameOperationRefactoring) visitorRef).getRenamedOperation();
        // Get the name of the methods before they are refactored
        String originalLeftName = leftOriginalOperation.getName();
        String originalRightName = rightOriginalOperation.getName();
        // Get the name of the methods after they are refactored
        String leftName = leftOperation.getName();
        String rightName = rightOperation.getName();
        // Get the names of the classes that the methods are in
        String leftClass = leftOperation.getClassName();
        String rightClass = rightOperation.getClassName();

        // Debug info to determine if the logic is correct
        System.out.println("Original Left Name: " + originalLeftName + " | Original Right Name: " + originalRightName);
        System.out.println("New Left Name: " + leftName + " | New Right Name: " + rightName);
        System.out.println("Left Class: " + leftClass + " | Right Class: " + rightClass);

        // If the methods are in different classes, check if they override
        if(!leftClass.equals(rightClass)) {
            return methodInheritanceConflict(elementRef, visitorRef);
        }
        // If the methods have the same name and different parameters in the same class, check for overloading
        else if(originalLeftName.equals(originalRightName) && !leftName.equals(rightName) &&
                !leftOperation.equalParameters(rightOperation)) {
            System.out.println("Overloading Conflict");
            return true;
        }
        // If the original method names are equal but the destination names are not equal, check for conflict
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

}
