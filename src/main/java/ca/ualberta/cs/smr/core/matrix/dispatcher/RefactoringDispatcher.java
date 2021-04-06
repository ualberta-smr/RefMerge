package ca.ualberta.cs.smr.core.matrix.dispatcher;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import com.intellij.openapi.project.Project;

public abstract class RefactoringDispatcher implements Dispatcher {
    Project project;
    Node dispatcherNode;

    public void set(Node dispatcherNode, Project project) {
        this.dispatcherNode = dispatcherNode;
        this.project = project;
    }

    public Node getNode() {
        return dispatcherNode;
    }
}

