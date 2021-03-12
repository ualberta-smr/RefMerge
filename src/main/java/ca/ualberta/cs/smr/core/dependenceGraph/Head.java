package ca.ualberta.cs.smr.core.dependenceGraph;

import org.refactoringminer.api.Refactoring;

public class Head {
    private String className;

    public Head() {
        this.className = null;
    }

    public void update(Node node) {
        if(className == null) {
            Refactoring refactoring = node.getRefactoring();
            this.className = refactoring.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        }
    }

    public String getClassName() {
        return this.className;
    }
}
