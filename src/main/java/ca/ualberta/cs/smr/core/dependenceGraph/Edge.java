package ca.ualberta.cs.smr.core.dependenceGraph;

public class Edge {
    Node node1;
    Node node2;
    int weight;

    Edge(Node node1, int weight, Node node2) {
        this.node1 = node1;
        this.weight = weight;
        this.node2 = node2;
    }

    public Node getSource() {
        return node1;
    }

    public Node getDestination() {
        return node2;
    }

    public int getWeight() {
        return weight;
    }

    public void updateWeight(int weight) {
        this.weight = weight;
    }

    public void updateDestination(Node newNode2) {
        this.node2 = newNode2;
    }
}
