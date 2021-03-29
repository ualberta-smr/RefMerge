package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;

public interface Element {
    void accept(RefactoringVisitor v);
}

