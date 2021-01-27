package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameMethodVisitor extends RefactoringVisitor {
    Refactoring visitorRef;

    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        boolean isConflicting = renameMethod.checkRenameMethodConflict(visitorRef);
    }

    @Override
    public void visit(RenameClassElement renameClass) {

    }
}
