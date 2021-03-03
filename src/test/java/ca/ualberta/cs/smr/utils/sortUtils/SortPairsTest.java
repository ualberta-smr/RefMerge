package ca.ualberta.cs.smr.utils.sortUtils;

import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import ca.ualberta.cs.smr.utils.sortingUtils.SortPairs;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SortPairsTest {

    @Test
    public void testSort() {
        List<Pair> pairsToSort = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            pairsToSort.add(new Pair(i, null));
        }
        List<Pair> sortedList = SortPairs.sortList(pairsToSort);
        Assert.assertTrue(isSorted(sortedList));

    }

    private boolean isSorted(List<Pair> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            if(list.get(i).getCommit() < list.get(i+1).getCommit()) {
                return false;
            }
        }
        return true;
    }
}
