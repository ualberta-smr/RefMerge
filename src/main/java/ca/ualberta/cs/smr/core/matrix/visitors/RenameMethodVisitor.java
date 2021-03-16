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
        if(graph.isSameBranch()) {
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
        if (elementNode != null) {
            // If there is dependence between branches, the rename method needs to happen before the rename class
            if (!graph.isSameBranch()) {
                graph.updateGraph(visitorNode, elementNode);
            } else {
                // If there is dependence on the same branch and the class and method are renamed in the same commit
                // or the method rename happens after, the rename method depends on the rename class
                if (elementNode.getCommit() <= visitorNode.getCommit()) {
                    graph.updateGraph(visitorNode, elementNode);
                }
                // Otherwise the rename class depends on the rename method in the same branch
                else {
                    graph.updateGraph(elementNode, visitorNode);
                }
            }
        }
    }
}
