package ca.ualberta.cs.smr.core.matrix.elements;


import org.refactoringminer.api.Refactoring;

public abstract class RefactoringElement implements Element {
    Refactoring elementRef;
    String path;


    public void set(Refactoring ref, String projectPath) {
        elementRef = ref;
        path = projectPath;
    }
}
