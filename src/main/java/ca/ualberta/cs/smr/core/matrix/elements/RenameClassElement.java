package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.matrix.conflictCheckers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.dependenceCheckers.DependenceCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;
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

    /*
     * Check if a rename class refactoring depends on a rename method refactoring to be performed first.
     */
    public boolean checkRenameMethodDependence(Refactoring visitorRef) {
        return DependenceCheckers.checkRenameMethodRenameClassDependence(elementRef, visitorRef);
    }

}
