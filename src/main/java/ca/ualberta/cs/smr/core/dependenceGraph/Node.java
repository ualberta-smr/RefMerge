package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Node> adjacentNodes;

    Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.adjacentNodes = new ArrayList<>();
    }

    public Refactoring getNode() {
        return refactoring;
    }

    public void addNeighbor(Node adjacent) {
        this.adjacentNodes.add(adjacent);
    }

    public boolean hasNeighbors() {
        return !adjacentNodes.isEmpty();
    }

    public List<Node> getNeighbors() {
        return adjacentNodes;
    }
}
