package ca.ualberta.cs.smr.refmerge.utils;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;


public class RefactoringObjectUtils {

    /*
     * Creates the refactoring object from the RefMiner refactoring object.
     */
    public static RefactoringObject createRefactoringObject(Refactoring refactoring) {
        switch(refactoring.getRefactoringType()) {
            case RENAME_CLASS:
            case MOVE_CLASS:
            case MOVE_RENAME_CLASS:
                return new MoveRenameClassObject(refactoring);
            case RENAME_METHOD:
            case MOVE_OPERATION:
            case MOVE_AND_RENAME_OPERATION:
                return new MoveRenameMethodObject(refactoring);
            case EXTRACT_OPERATION:
                return new ExtractMethodObject(refactoring);
            case INLINE_OPERATION:
                return new InlineMethodObject(refactoring);

        }
        return null;
    }

    /*
     * Inserts the new refactoring object into the respective position.
     */
    public static void insertRefactoringObject(RefactoringObject refactoringObject,
                                               ArrayList<RefactoringObject> refactoringObjects, boolean forReplay) {
        int newRefactoringValue = refactoringObject.getRefactoringOrder().getOrder();
        // Add the new refactoring object to the end of the list
        refactoringObjects.add(refactoringObject);

        int index = 0;
        for(index = refactoringObjects.size()-1; index > 0; index--) {
            RefactoringObject existingRefactoring = refactoringObjects.get(index-1);
            int existingRefactoringValue = existingRefactoring.getRefactoringOrder().getOrder();
            if(forReplay) {
                if (newRefactoringValue > existingRefactoringValue) {
                    refactoringObjects.set(index, existingRefactoring);
                } else {
                    break;
                }
            }
            else {
                if (newRefactoringValue <= existingRefactoringValue) {
                    refactoringObjects.set(index, existingRefactoring);
                } else {
                    break;
                }
            }
        }
        refactoringObjects.set(index, refactoringObject);
    }
}
