package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

/*
 * The RefactoringVisitor superclass contains each visit method that the visitor classes will need to use. Each time we
 * add a new refactoring type, we need to add a new visit method with the corresponding refactoring element.
 */

public class RefactoringVisitor {
    Node visitorNode;
    DependenceGraph graph;

    public void set(Node visitorNode, DependenceGraph graph) {
        this.visitorNode = visitorNode;
        this.graph = graph;
    }

    public void visit(RenameMethodElement e) {

    }

    public void visit(RenameClassElement e) {

    }

    public void visit(ExtractMethodElement e) {

    }


}
