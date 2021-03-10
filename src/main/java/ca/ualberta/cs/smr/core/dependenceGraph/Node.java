package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Edge> adjacentNodes;
    private List<Node> dependsList;


    Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.adjacentNodes = new ArrayList<>();
        this.dependsList = new ArrayList<>();

    }

    public Refactoring getRefactoring() {
        return refactoring;
    }

    public void addEdge(Edge edge) {
        this.adjacentNodes.add(edge);
    }

    public boolean hasNeighbors() {
        return !adjacentNodes.isEmpty();
    }

    public List<Edge> getEdges() {
        return adjacentNodes;
    }

    public void addToDependsList(Node node) {
        dependsList.add(node);
    }

    public List<Node> dependsOn() {
        return dependsList;
    }

}
