package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import org.refactoringminer.api.Refactoring;

public abstract class RefactoringElement implements Element {
    Refactoring elementRef;


    public void set(Refactoring ref) {
        elementRef = ref;
    }
}
