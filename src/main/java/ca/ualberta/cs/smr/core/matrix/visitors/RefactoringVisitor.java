package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import org.refactoringminer.api.Refactoring;

public abstract class RefactoringVisitor implements Visitor {
    Refactoring visitorRef;
    Graph graph;

    public void set(Refactoring visitorRef, Graph graph) {
        this.visitorRef = visitorRef;
        this.graph = graph;
    }

}
