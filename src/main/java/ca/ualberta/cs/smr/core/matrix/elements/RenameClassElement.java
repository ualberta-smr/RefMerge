package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.conflictCheckers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.dependenceCheckers.DependenceCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;




public class RenameClassElement extends RefactoringElement {
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

    /*
     * Check if a rename class refactoring depends on a rename method refactoring to be performed first.
     */
    public Node checkRenameMethodDependence(Refactoring visitorRef) {
        if(DependenceCheckers.checkRenameMethodRenameClassDependence(elementRef, visitorRef)) {
            return elementNode;
        }
        return null;
    }

}
