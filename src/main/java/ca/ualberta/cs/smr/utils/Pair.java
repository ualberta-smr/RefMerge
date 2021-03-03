package ca.ualberta.cs.smr.utils;

import org.refactoringminer.api.Refactoring;

public class Pair {
    int commit;
    Refactoring refactoring;

    public Pair(int commit, Refactoring refactoring) {
        commit = commit;
        this.refactoring = refactoring;
    }

    public int getCommit() {
        return commit;
    }

    public Refactoring getValue() {
        return refactoring;
    }
}
