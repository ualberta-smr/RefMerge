package ca.ualberta.cs.smr.utils.sortingUtils;

import java.util.List;

public class SortPairs {

    public static void sortList(List<Pair> list) {
        list.sort(new ChainedComparator(new CommitComparator(), new RefactoringComparator()));
    }
}


