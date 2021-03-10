package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Edge> adjacentNodes;
    private boolean visited;

    Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.adjacentNodes = new ArrayList<>();
        visited = false;
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

    public void visit() {
        visited = true;
    }

    public boolean wasVisited() {
        return visited;
    }
}
