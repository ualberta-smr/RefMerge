package ca.ualberta.cs.smr.utils.sortUtils;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.sortingUtils.CommitComparator;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import ca.ualberta.cs.smr.utils.sortingUtils.RefactoringComparator;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SortUtilsTests {

    private static final HashMap<RefactoringType, Integer> refactoringTypeMap =
            new HashMap<RefactoringType, Integer>() {{
                put(RefactoringType.RENAME_METHOD, 0);
                put(RefactoringType.RENAME_CLASS, 1);
    }};

    @Test
    public void testCommitComparator() {
        List<Pair> pairsToSort = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            pairsToSort.add(new Pair(i, null));
        }
        pairsToSort.sort(new CommitComparator());
        Assert.assertTrue(isSorted(pairsToSort));

    }

    @Test
    public void testRefactoringTypeComparator() {
        List<Pair> typesToSort = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            Refactoring ref = GetDataForTests.getEmptyRefactoring(RefactoringType.RENAME_CLASS);
            Pair pair = new Pair(0, ref);
            typesToSort.add(pair);
            ref = GetDataForTests.getEmptyRefactoring(RefactoringType.RENAME_METHOD);
            pair = new Pair(0, ref);
            typesToSort.add(pair);
        }
        typesToSort.sort(new RefactoringComparator());
        Assert.assertTrue(isRefactoringListSorted(typesToSort));
    }

    private boolean isRefactoringListSorted(List<Pair> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            RefactoringType type1 = list.get(i).getValue().getRefactoringType();
            RefactoringType type2 = list.get(i).getValue().getRefactoringType();
            if(refactoringTypeMap.get(type1) < refactoringTypeMap.get(type2)) {
                return false;
            }
        }
        return true;
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
