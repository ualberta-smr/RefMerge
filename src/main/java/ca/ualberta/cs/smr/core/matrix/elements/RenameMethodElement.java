package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers.*;

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

        // Check for a method override conflict
        if(checkOverrideConflict(elementRef, visitorRef)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(checkOverloadConflict(elementRef, visitorRef)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(checkMethodNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }


}
