package ca.ualberta.cs.smr.utils.sortingUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortPairs {

    public static List<Pair> sortList(List<Pair> list) {

        list.sort(new CommitComparator());
        return list;
    }
}

class CommitComparator implements Comparator<Pair> {

    @Override
    public int compare(Pair pair1, Pair pair2) {
        int commit1 = pair1.getCommit();
        int commit2 = pair2.getCommit();
        return - (commit1 - commit2);
    }
}
