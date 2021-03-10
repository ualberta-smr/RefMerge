package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameClassVisitor extends RefactoringVisitor {
    Refactoring visitorRef;
    boolean foundDependence;


    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    public boolean getDependenceResult() {
        return foundDependence;
    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        foundDependence = renameMethod.checkRenameClassDependence(visitorRef);
        System.out.println("Rename Method/Rename Class does not conflict");
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        boolean foundConflict = renameClass.checkRenameClassConflict(visitorRef);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }
}
