package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.matrix.conflictCheckers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;




public class RenameClassElement extends RefactoringElement {
    Refactoring elementRef;
    Project project;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Refactoring ref, Project project) {
        elementRef = ref;
        this.project = project;
    }

    /*
     * Check if rename class conflicts with rename class
     */
    public boolean checkRenameClassConflict(Refactoring visitorRef) {
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        // Check class naming conflict
        if(conflictCheckers.checkClassNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    public boolean checkRenameMethodDependence(Refactoring visitorRef) {
        String elementClass = ((RenameClassRefactoring) elementRef).getOriginalClass().getName();
        String visitorClass = ((RenameOperationRefactoring) visitorRef).getOriginalOperation().getClassName();
        return elementClass.equals(visitorClass);
    }

}
