package ca.ualberta.cs.smr.utils.sortUtils;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.sortingUtils.*;
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
        Assert.assertTrue(isCommitValueSorted(pairsToSort));

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

    @Test
    public void testSortPairs() {
        List<Pair> pairsToSort = new ArrayList<>();
        for(int count = 0; count < 10; count++) {
            Refactoring ref = GetDataForTests.getEmptyRefactoring(RefactoringType.RENAME_METHOD);
            Pair pair = new Pair(count, ref);
            pairsToSort.add(pair);
            ref = GetDataForTests.getEmptyRefactoring(RefactoringType.RENAME_CLASS);
            pair = new Pair(count, ref);
            pairsToSort.add(pair);
        }
        SortPairs.sortList(pairsToSort);
        Assert.assertTrue(isRefactoringAndCommitSorted(pairsToSort));
    }

    private boolean isRefactoringAndCommitSorted(List<Pair> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            int commit1 = list.get(i).getCommit();
            int commit2 = list.get(i+1).getCommit();
            RefactoringType type1 = list.get(i).getValue().getRefactoringType();
            RefactoringType type2 = list.get(i+1).getValue().getRefactoringType();
            if(commit1 < commit2) {
                return false;
            }
            else if(commit1 == commit2) {
                if(refactoringTypeMap.get(type1) < refactoringTypeMap.get(type2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isRefactoringListSorted(List<Pair> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            RefactoringType type1 = list.get(i).getValue().getRefactoringType();
            RefactoringType type2 = list.get(i + 1).getValue().getRefactoringType();
            if(refactoringTypeMap.get(type1) < refactoringTypeMap.get(type2)) {
                return false;
            }
        }
        return true;
    }
    private boolean isCommitValueSorted(List<Pair> list) {
        for(int i = 0; i < list.size() - 1; i++) {
            if(list.get(i).getCommit() < list.get(i+1).getCommit()) {
                return false;
            }
        }
        return true;
    }
}
