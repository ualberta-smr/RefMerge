package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers.checkClassNamingConflict;


public class RenameClassElement extends RefactoringElement {
    Refactoring elementRef;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Refactoring ref) {
        elementRef = ref;
    }

    /*
     * Check if rename class conflicts with rename class
     */
    public boolean checkRenameClassConflict(Refactoring visitorRef) {

        // get the package of each class
        String leftPackage = ((RenameClassRefactoring) elementRef).getOriginalClass().getPackageName();
        String rightPackage = ((RenameClassRefactoring) visitorRef).getOriginalClass().getPackageName();

        // If the refactored classes are in different classes, check if inheritance conflict occurs
        if(!leftPackage.equals(rightPackage)) {
            return classInheritanceConflict(elementRef, visitorRef);
        }
        // Check class naming conflict
        if(checkClassNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
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
