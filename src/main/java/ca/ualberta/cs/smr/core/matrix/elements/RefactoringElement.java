package ca.ualberta.cs.smr.core.matrix.elements;


import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;

public abstract class RefactoringElement implements Element {
    Refactoring elementRef;
    Project project;


    public void set(Refactoring ref, Project project) {
        elementRef = ref;
        this.project = project;
    }
}
