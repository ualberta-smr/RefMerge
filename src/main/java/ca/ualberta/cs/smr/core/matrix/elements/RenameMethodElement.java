package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import org.refactoringminer.api.Refactoring;

public class RenameMethodElement extends RefactoringElement {
    Refactoring elementRef;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Refactoring ref) {
        elementRef = ref;
    }

    public void checkRenameMethodConflict() {

    }
}
