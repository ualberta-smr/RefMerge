package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;

public abstract class RefactoringElement implements Element {
    Refactoring elementRef;
    Project project;
    Node elementNode;

    public void set(Node elementNode, Project project) {
        this.elementNode = elementNode;
        this.elementRef = elementNode.getRefactoring();
        this.project = project;
    }
}

