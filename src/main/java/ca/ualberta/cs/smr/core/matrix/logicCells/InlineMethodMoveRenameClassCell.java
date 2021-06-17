package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class InlineMethodMoveRenameClassCell {

    /*
     * Check if inline method and move+rename class refactorings have dependence between branches
     */
    public static boolean checkDependence(RefactoringObject moveRenameClass, RefactoringObject inlineMethod) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) moveRenameClass;
        InlineMethodObject inlineMethodObject = (InlineMethodObject) inlineMethod;

        String moveRenameOriginalClass = moveRenameClassObject.getOriginalClassObject().getClassName();
        String inlineDestinationClass = inlineMethodObject.getDestinationClassName();

        // If the method is inlined to a method in the class that is refactored, there is dependence.
        return moveRenameOriginalClass.equals(inlineDestinationClass);
    }
}
