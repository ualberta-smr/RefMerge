package ca.ualberta.cs.smr.core.matrix.visitors;

import org.refactoringminer.api.Refactoring;

public abstract class RefactoringVisitor implements Visitor {
    Refactoring visitorRef;
    boolean foundDependence;


    public void set(Refactoring ref) {
        visitorRef = ref;
    }

    public boolean getDependenceResult() {
        return foundDependence;
    }

}
