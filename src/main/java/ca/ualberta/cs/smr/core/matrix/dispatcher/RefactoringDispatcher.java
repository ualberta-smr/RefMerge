package ca.ualberta.cs.smr.core.matrix.dispatcher;


import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public abstract class RefactoringDispatcher implements Dispatcher {
    Project project;
    RefactoringObject refactoringObject;

    public void set(RefactoringObject refactoringObject, Project project) {
        this.refactoringObject = refactoringObject;
        this.project = project;
    }

    public void setRefactoringObject(RefactoringObject refactoringObject) {
        this.refactoringObject = refactoringObject;
    }

    public RefactoringObject getRefactoringObject() {
        return this.refactoringObject;
    }

    public Project getProject() {
        return this.project;
    }
}

