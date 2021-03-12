package ca.ualberta.cs.smr.core.dependenceGraph;

public class Edge {
    Node node1;
    Node node2;

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getSource() {
        return node1;
    }

    public Node getDestination() {
        return node2;
    }

}
