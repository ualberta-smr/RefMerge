package ca.ualberta.cs.smr.core.matrix.visitors;

import org.refactoringminer.api.Refactoring;

public class RefactoringVisitor implements Visitor {
    Refactoring visitorRef;

    public void set(Refactoring ref) {
        visitorRef = ref;
    }
}
