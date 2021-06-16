package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;

public class InlineMethodMoveRenameMethodCell {

    /*
     * Check if inline method and move+rename method refactorings conflict between branches.
     */
    public static boolean inlineMethodMoveRenameMethodConflictCell(RefactoringObject moveRenameMethod, RefactoringObject inlineMethod) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) moveRenameMethod;
        InlineMethodObject inlineMethodObject = (InlineMethodObject) inlineMethod;

        MethodSignatureObject moveRenameOriginalMethod = moveRenameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject inlineOriginalMethod = ((InlineMethodObject) inlineMethod).getOriginalMethodSignature();
        String moveRenameOriginalClass = moveRenameMethodObject.getOriginalClassName();
        String inlineOriginalClass = inlineMethodObject.getOriginalClassName();

        // If the same method is inlined and the method is renamed/moved, this is a conflict.
        if(!moveRenameOriginalMethod.equalsSignature(inlineOriginalMethod) || !moveRenameOriginalClass.equals(inlineOriginalClass)) {
            return false;
        }

        return true;
    }
}
