package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import org.refactoringminer.api.Refactoring;

public class RefactoringElement implements Element {
    Refactoring elementRef;

    @Override
    public void accept(Visitor v) {
        v.visit();
    }

    public void set(Refactoring ref) {
        elementRef = ref;
    }
}
