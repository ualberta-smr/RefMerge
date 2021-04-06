package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import com.intellij.openapi.project.Project;

/*
 * The Receiver superclass contains each receive method that the receiver classes will need to use. Each time we
 * add a new refactoring type, we need to add a new receive method with the corresponding refactoring element.
 */

public class Receiver {
    Node receiverNode;
    DependenceGraph graph;
    Project project;

    public void set(Node receiverNode, DependenceGraph graph, Project project) {
        this.receiverNode = receiverNode;
        this.graph = graph;
        this.project = project;
    }

    public void receive(RenameMethodDispatcher dispatcher) {

    }

    public void receive(RenameClassDispatcher dispatcher) {

    }

    public void receive(ExtractMethodDispatcher dispatcher) {

    }


}
