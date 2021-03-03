package ca.ualberta.cs.smr.utils.sortingUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ChainedComparator implements Comparator<Pair> {

    private final List<Comparator<Pair>> listComparators;


    @SafeVarargs
    public ChainedComparator(Comparator<Pair>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(Pair pair1, Pair pair2) {
        for(Comparator<Pair> comparator : listComparators) {
            int result = comparator.compare(pair1, pair2);
            if(result != 0) {
                return result;
            }
        }
        return 0;
    }
}
