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
            case RENAME_ATTRIBUTE:
            case MOVE_ATTRIBUTE:
            case MOVE_RENAME_ATTRIBUTE:
                return new MoveRenameFieldObject(refactoring);
            case PULL_UP_OPERATION:
                return new PullUpMethodObject(refactoring);
            case PUSH_DOWN_OPERATION:
                return new PushDownMethodObject(refactoring);
            case PULL_UP_ATTRIBUTE:
                return new PullUpFieldObject(refactoring);
            case PUSH_DOWN_ATTRIBUTE:
                return new PushDownFieldObject(refactoring);
            case RENAME_PACKAGE:
                return new RenamePackageObject(refactoring);
            case RENAME_PARAMETER:
                return new RenameParameterObject(refactoring);
            // Only used for simplification logic currently. Needed for improving method level refactoring resilience
            case ADD_PARAMETER:
                return new AddParameterObject(refactoring);
            // Only used for simplification logic currently. Needed for improving method level refactoring resilience
            case REMOVE_PARAMETER:
                return new RemoveParameterObject(refactoring);
            // Only used for simplification logic currently. Needed for improving method level refactoring resilience
            case REORDER_PARAMETER:
                return new ReorderParameterObject(refactoring);
            // Only used for simplification logic currently. Needed for improving method level refactoring resilience
            case CHANGE_PARAMETER_TYPE:
                return new ChangeParameterTypeObject(refactoring);

        }
        return null;
    }

    /*
     * Inserts the new refactoring object into the respective position.
     */
    public static void insertRefactoringObject(RefactoringObject refactoringObject,
                                               ArrayList<RefactoringObject> refactoringObjects, boolean forReplay) {
        RefactoringOrder refactoringOrder = refactoringObject.getRefactoringOrder();
        // If null, it is only used for combination/transitivity and will not be inverted or replayed
        if(refactoringOrder == null) {
            return;
        }
        int newRefactoringValue = refactoringOrder.getOrder();

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
