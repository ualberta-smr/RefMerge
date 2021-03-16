package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;

public abstract class RefactoringVisitor implements Visitor {
    Node visitorNode;
    DependenceGraph graph;

    public void set(Node visitorNode, DependenceGraph graph) {
        this.visitorNode = visitorNode;
        this.graph = graph;
    }

}
