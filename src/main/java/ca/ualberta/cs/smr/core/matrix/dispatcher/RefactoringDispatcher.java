package ca.ualberta.cs.smr.core.matrix.dispatcher;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public abstract class RefactoringDispatcher implements Dispatcher {
    Project project;
    Node dispatcherNode;
    RefactoringObject refactoringObject;

    public void set(Node dispatcherNode, Project project) {
        this.dispatcherNode = dispatcherNode;
        this.project = project;
    }

    public void set(RefactoringObject refactoringObject, Project project) {
        this.refactoringObject = refactoringObject;
        this.project = project;
        this.dispatcherNode = null;
    }

    public Node getNode() {
        return dispatcherNode;
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

