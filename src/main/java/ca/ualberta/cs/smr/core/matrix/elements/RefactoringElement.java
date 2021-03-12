package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import com.intellij.openapi.project.Project;

public abstract class RefactoringElement implements Element {
    Project project;
    Node elementNode;

    public void set(Node elementNode, Project project) {
        this.elementNode = elementNode;
        this.project = project;
    }
}

