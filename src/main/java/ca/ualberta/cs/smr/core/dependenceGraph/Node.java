package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Refactoring refactoring;
    private List<Node> dependsList;
    private Head head;

    public Node(Refactoring refactoring) {
        this.refactoring = refactoring;
        this.dependsList = new ArrayList<>();
        this.head = new Head();
    }

    public Refactoring getRefactoring() {
        return refactoring;
    }

    public void addToDependsList(Node node) {
        dependsList.add(node);
    }

    public String getDependenceChainClassHead() {
        if(head.getClassName() == null) {
            return refactoring.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        }
        return head.getClassName();
    }
}
