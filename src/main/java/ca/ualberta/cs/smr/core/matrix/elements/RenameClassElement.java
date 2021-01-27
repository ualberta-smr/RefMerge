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

        // Check class naming conflict
        if(checkClassNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

}
