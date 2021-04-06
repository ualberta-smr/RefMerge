package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

/*
 * The Receiver superclass contains each receive method that the receiver classes will need to use. Each time we
 * add a new refactoring type, we need to add a new receive method with the corresponding refactoring element.
 */

public class Receiver {
    Node receiverNode;
    DependenceGraph graph;

    public void set(Node visitorNode, DependenceGraph graph) {
        this.receiverNode = visitorNode;
        this.graph = graph;
    }

    public void receive(RenameMethodElement e) {

    }

    public void receive(RenameClassElement e) {

    }

    public void receive(ExtractMethodElement e) {

    }


}
