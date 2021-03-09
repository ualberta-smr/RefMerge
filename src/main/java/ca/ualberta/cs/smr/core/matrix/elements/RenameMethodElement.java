package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.conflictCheckers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;


/*
 * Checks if visitor refactorings confict with a rename method refactoring.
 */
public class RenameMethodElement extends RefactoringElement {
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
     *  Check if a rename method refactoring conflicts with a second rename method refactoring.
     */
    public boolean checkRenameMethodConflict(Refactoring visitorRef) {
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        // Check for a method override conflict
        if(conflictCheckers.checkOverrideConflict(elementRef, visitorRef)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(conflictCheckers.checkOverloadConflict(elementRef, visitorRef)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(conflictCheckers.checkMethodNamingConflict(elementRef, visitorRef)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    public boolean checkRenameClassDependence(Refactoring visitorRef) {
        String elementClass = ((RenameClassRefactoring) elementRef).getOriginalClass().getName();
        String visitorClass = ((RenameOperationRefactoring) visitorRef).getOriginalOperation().getClassName();
        return elementClass.equals(visitorClass);
    }

}
