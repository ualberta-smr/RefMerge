package ca.ualberta.cs.smr.utils.sortingUtils;

import org.refactoringminer.api.Refactoring;

public class Pair {
    private final int commit;
    private Refactoring refactoring;

    public Pair(int commit, Refactoring refactoring) {
        this.commit = commit;
        this.refactoring = refactoring;
    }

    public int getCommit() {
        return commit;
    }

    public Refactoring getValue() {
        return refactoring;
    }

    public void setValue(Refactoring refactoring) {
        this.refactoring = refactoring;
    }
}
