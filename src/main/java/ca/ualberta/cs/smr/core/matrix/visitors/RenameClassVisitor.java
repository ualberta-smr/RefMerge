package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameClassVisitor extends RefactoringVisitor {
    Refactoring visitorRef;

    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        System.out.println("Rename Method/Rename Class does not conflict");
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        boolean isConflicting = renameClass.checkRenameClassConflict(visitorRef);
        System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
    }
}
