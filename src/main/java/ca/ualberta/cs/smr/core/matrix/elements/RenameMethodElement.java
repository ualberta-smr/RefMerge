package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.conflictCheckers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.dependenceCheckers.DependenceCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;


/*
 * Checks if visitor refactorings confict with a rename method refactoring.
 */
public class RenameMethodElement extends RefactoringElement {
    Refactoring elementRef;
    Project project;
    Node elementNode;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Node elementNode, Project project) {
        this.elementNode = elementNode;
        this.elementRef = elementNode.getRefactoring();
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

    /*
     * Check if a rename class refactoring depends on a rename method refactoring to be performed first.
     */
    public Refactoring checkRenameClassDependence(Refactoring visitorRef) {
        if(DependenceCheckers.checkRenameMethodRenameClassDependence(visitorRef, elementRef)) {
            return elementRef;
        }
        return null;
    }

}
