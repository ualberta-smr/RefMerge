package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Edge> adjacentNodes;

    Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.adjacentNodes = new ArrayList<>();
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

}
