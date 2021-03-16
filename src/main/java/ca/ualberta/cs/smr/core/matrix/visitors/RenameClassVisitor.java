package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

public class RenameClassVisitor extends RefactoringVisitor {
    Node visitorNode;
    DependenceGraph graph;

    public void set(Node visitorNode, DependenceGraph graph) {
        this.visitorNode = visitorNode;
        this.graph = graph;
    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        Node elementNode = renameMethod.checkRenameClassDependence(visitorNode);
        if(elementNode != null) {
            graph.updateGraph(elementNode, visitorNode);
        }
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        boolean foundConflict = renameClass.checkRenameClassConflict(visitorNode);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }
}
