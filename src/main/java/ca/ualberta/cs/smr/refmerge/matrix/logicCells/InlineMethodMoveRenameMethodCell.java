package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;

public class InlineMethodMoveRenameMethodCell {

    /*
     * Check if inline method and move+rename method refactorings conflict between branches.
     */
    public static boolean conflictCell(RefactoringObject moveRenameMethod, RefactoringObject inlineMethod) {
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

    /*
     * Check if inline method and move+rename method refactorings have dependence between branches.
     */
    public static boolean dependenceCell(RefactoringObject moveRenameMethod, RefactoringObject inlineMethod) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) moveRenameMethod;
        InlineMethodObject inlineMethodObject = (InlineMethodObject) inlineMethod;

        MethodSignatureObject moveRenameOriginalMethod = moveRenameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject inlineDestinationMethod = ((InlineMethodObject) inlineMethod).getDestinationMethodSignature();
        String moveRenameOriginalClass = moveRenameMethodObject.getOriginalClassName();
        String inlineDestinationClass = inlineMethodObject.getDestinationClassName();

        // If the target method in the inline method refactoring was moved/renamed, update the target method to the new method
        if(!moveRenameOriginalMethod.equalsSignature(inlineDestinationMethod) || !moveRenameOriginalClass.equals(inlineDestinationClass)) {
            return false;
        }
        return true;
    }

    /*
     * Check if inline method and move+rename method refactorings have transitivity on the same branch.
     */
    public static boolean checkCombination(RefactoringObject moveRenameMethod, RefactoringObject inlineMethod) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) moveRenameMethod;
        InlineMethodObject inlineMethodObject = (InlineMethodObject) inlineMethod;

        MethodSignatureObject moveRenameOriginalMethod = moveRenameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject inlineDestinationMethod = ((InlineMethodObject) inlineMethod).getDestinationMethodSignature();
        String moveRenameOriginalClass = moveRenameMethodObject.getOriginalClassName();
        String inlineDestinationClass = inlineMethodObject.getDestinationClassName();

        // If the target method for the inline method refactoring is moved/renamed, update the target method to the
        // destination method for the move+rename method refactoring
        if(!moveRenameOriginalMethod.equalsSignature(inlineDestinationMethod) || !moveRenameOriginalClass.equals(inlineDestinationClass)) {
            return false;
        }
        inlineMethod.setDestinationFilePath(moveRenameMethod.getDestinationFilePath());
        ((InlineMethodObject) inlineMethod).setDestinationClassName(moveRenameMethodObject.getDestinationClassName());
        ((InlineMethodObject) inlineMethod).setDestinationMethodSignature(moveRenameMethodObject.getDestinationMethodSignature());
        return true;
    }

}
