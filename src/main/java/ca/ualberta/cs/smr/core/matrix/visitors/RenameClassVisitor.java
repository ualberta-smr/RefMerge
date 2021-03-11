package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameClassVisitor extends RefactoringVisitor {
    Node visitorNode;
    Refactoring visitorRef;
    Graph graph;

    public void set(Node visitorNode, Graph graph) {
        this.visitorNode = visitorNode;
        this.visitorRef = visitorNode.getRefactoring();
        this.graph = graph;
    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        Node elementNode = renameMethod.checkRenameClassDependence(visitorRef);
        if(elementNode != null) {
            graph.updateGraph(elementNode, visitorNode);
        }
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        boolean foundConflict = renameClass.checkRenameClassConflict(visitorRef);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }
}
