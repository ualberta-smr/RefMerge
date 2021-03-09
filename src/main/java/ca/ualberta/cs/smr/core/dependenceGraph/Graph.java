package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private List<Node> nodes;

    public Graph(List<Pair> pairs) {
        this.nodes = new ArrayList<>();
        if(pairs.size() == 0) {
            return;
        }
        if(pairs.size() == 1) {
            Node node = new Node(pairs.get(0).getValue());
            addNode(node);
            return;
        }
        for(int i = pairs.size() - 1; i > -1; i--) {
        //for(Pair pair : pairs) {
            Pair pair = pairs.get(i);
            Refactoring refactoring = pair.getValue();
            Node node = new Node(refactoring);
            traverseGraph(node);
            addNode(node);
        }

    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    void addEdge(Node from, Node to) {
        from.addNeighbor(to);
    }

    void traverseGraph(Node newNode) {
        for(Node node : nodes) {
            if(!node.hasNeighbors()) {
                addEdge(node, newNode);
            }
            // Check for dependence
            // If dependence, change edge or add strong edge
            // Then when checking for conflict in Matrix, check if chain?
        }

    }

    void printGraph() {
        for(Node node : nodes) {
            for(Node neighbor : node.getNeighbors()) {
                System.out.println(node.getNode().toString() + " <-- " + neighbor.getNode().toString());
            }
        }
    }


}
