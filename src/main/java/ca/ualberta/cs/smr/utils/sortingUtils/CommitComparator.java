package ca.ualberta.cs.smr.utils.sortingUtils;

import java.util.Comparator;

public class CommitComparator implements Comparator<Pair> {

    @Override
    public int compare(Pair pair1, Pair pair2) {
        int commit1 = pair1.getCommit();
        int commit2 = pair2.getCommit();
        return - (commit1 - commit2);
    }
}