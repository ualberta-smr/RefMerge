package ca.ualberta.cs.smr.utils.sortingUtils;

import java.util.Comparator;
import java.util.List;

public class SortPairs {

    public static List<Pair> sortList(List<Pair> list) {

        list.sort(new CommitComparator());
        return list;
    }
}


