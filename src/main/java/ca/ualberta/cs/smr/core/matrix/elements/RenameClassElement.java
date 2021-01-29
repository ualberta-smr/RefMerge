package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.refactoringminer.api.Refactoring;




public class RenameClassElement extends RefactoringElement {
    Refactoring elementRef;
    String path;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Refactoring ref, String projectPath) {
        elementRef = ref;
        path = projectPath;
    }

    /*
     * Check if rename class conflicts with rename class
     */
    public boolean checkRenameClassConflict(Refactoring visitorRef) {
        ConflictCheckers conflictCheckers = new ConflictCheckers(path);
        // Check class naming conflict
        if(conflictCheckers.checkClassNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

}
