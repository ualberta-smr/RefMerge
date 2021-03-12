package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;

public abstract class RefactoringVisitor implements Visitor {
    Node visitorNode;
    Graph graph;

    public void set(Node visitorNode, Graph graph) {
        this.visitorNode = visitorNode;
        this.graph = graph;
    }

}
