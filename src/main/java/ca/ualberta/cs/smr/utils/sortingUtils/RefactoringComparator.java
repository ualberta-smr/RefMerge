package ca.ualberta.cs.smr.utils.sortingUtils;

import org.refactoringminer.api.RefactoringType;

import java.sql.Ref;
import java.util.Comparator;
import java.util.HashMap;

public class RefactoringComparator implements Comparator<Pair> {

    private static final HashMap<RefactoringType, Integer> refactoringTypeMap =
            new HashMap<RefactoringType, Integer>() {{
                put(RefactoringType.RENAME_METHOD, 0);
                put(RefactoringType.EXTRACT_OPERATION, 1);
                put(RefactoringType.RENAME_CLASS, 2);
            }};


    @Override
    public int compare(Pair pair1, Pair pair2) {
        RefactoringType type1 = pair1.getValue().getRefactoringType();
        RefactoringType type2 = pair2.getValue().getRefactoringType();
        int value1 = refactoringTypeMap.get(type1);
        int value2 = refactoringTypeMap.get(type2);
        return value1 - value2;
    }

}