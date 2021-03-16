package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

public class RenameMethodVisitor extends RefactoringVisitor {
    Node visitorNode;
    DependenceGraph graph;

    public void set(Node visitorNode, DependenceGraph graph) {
        this.visitorNode = visitorNode;
        this.graph = graph;
    }

    /*
     * Check if rename method conflicts with rename method
     */
    @Override
    public void visit(RenameMethodElement renameMethod) {
        if(!graph.containsVertex(visitorNode)) {
            return;
        }
        boolean foundConflict = renameMethod.checkRenameMethodConflict(visitorNode);
        System.out.println("Rename Method/Rename Method conflict: " + foundConflict);
    }

    /*
     * Check if rename class conflicts with rename method
     */
    @Override
    public void visit(RenameClassElement renameClass) {
        Node elementNode = renameClass.checkRenameMethodDependence(visitorNode);
        if(elementNode != null) {
            graph.updateGraph(visitorNode, elementNode);
        }
    }
}
