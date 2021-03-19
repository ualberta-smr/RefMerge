package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;

public class Node {
    private Refactoring refactoring;
    private ArrayList<Node> dependsList;
    private Head head;

    public Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.dependsList = new ArrayList<>();
        this.head = new Head();
    }

    public Refactoring getRefactoring() {
        return refactoring;
    }

    public void addDependsList(ArrayList<Node> nodes) {
        dependsList.addAll(nodes);
    }

    public ArrayList<Node> getDependsList() {
        return dependsList;
    }

    public String getDependenceChainClassHead() {
        if(head.getClassName() == null) {
            return refactoring.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        }
        return head.getClassName();
    }
}
