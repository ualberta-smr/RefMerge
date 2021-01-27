package ca.ualberta.cs.smr.core.matrix.visitors;

import org.refactoringminer.api.Refactoring;

public class RenameMethodVisitor extends RefactoringVisitor {
    Refactoring visitorRef;

    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    @Override
    public void visit() {

    }
}
