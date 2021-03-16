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
        if (elementNode != null) {
            // If there is dependence between branches, the rename method needs to happen before the rename class
            if(!graph.isSameBranch()) {
                graph.updateGraph(elementNode, visitorNode);
            }
            else {
                // If there is dependence on the same branch and the class and method are renamed in the same commit
                // or the method rename happens after, the rename method depends on the rename class
                if(elementNode.getCommit() >= visitorNode.getCommit()) {
                    graph.updateGraph(visitorNode, elementNode);
                }
                // Otherwise the rename class depends on the rename method in the same branch
                else {
                    graph.updateGraph(elementNode, visitorNode);
                }
            }
        }
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        // Check for rename class/rename class conflict if checking between branches
        if(!graph.isSameBranch()) {
            boolean foundConflict = renameClass.checkRenameClassConflict(visitorNode);
            System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
        }
        // Check for rename class/rename class dependence if checking the same branch
        else {
            Node elementNode = renameClass.checkRenameClassDependence(visitorNode);
            if(elementNode != null) {
                graph.updateGraph(elementNode, visitorNode);
            }
        }
    }
}
