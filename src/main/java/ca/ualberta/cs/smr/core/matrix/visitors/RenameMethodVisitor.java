package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameMethodVisitor extends RefactoringVisitor {
    Refactoring visitorRef;

    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    /*
     * Check if rename method conflicts with rename method
     */
    @Override
    public void visit(RenameMethodElement renameMethod) {
        boolean isConflicting = renameMethod.checkRenameMethodConflict(visitorRef);
        System.out.println("Rename Method/Rename Method conflict: " + isConflicting);
    }

    /*
     * Check if rename class conflicts with rename method
     */
    @Override
    public void visit(RenameClassElement renameClass) {
        boolean foundDependence = renameClass.checkRenameMethodDependence(visitorRef);
        System.out.println("Rename Method/Rename Class does not conflict");
    }
}
