package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Node> dependsList;
    private Head head;
    private int commit;
    private int edgeCount;

    public Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.dependsList = new ArrayList<>();
        this.head = new Head();
        this.commit = 0;
        this.edgeCount = 0;
    }

    public Node(Refactoring refactoring, int commit) {
        this.refactoring = refactoring;
        this.dependsList = new ArrayList<>();
        this.head = new Head();
        this.commit = commit;
        this.edgeCount = 0;
    }

    public Refactoring getRefactoring() {
        return refactoring;
    }

    public int getCommit() {
        return commit;
    }

    public void addToDependsList(Node node) {
        edgeCount++;
        dependsList.add(node);
    }

    public void updateHead(Node node) {
        head.update(node);
    }

    public String getDependenceChainClassHead() {
        if(head.getClassName() == null) {
            return refactoring.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        }
        return head.getClassName();
    }

    public boolean hasManyEdges() {
        if(edgeCount <= 1) {
            return false;
        }
        return true;
    }

    public void decreaseEdgeCount() {
        this.edgeCount--;
    }
}
