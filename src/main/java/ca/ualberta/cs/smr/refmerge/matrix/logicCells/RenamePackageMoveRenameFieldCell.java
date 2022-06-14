package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;

public class RenamePackageMoveRenameFieldCell {

    public static boolean checkCombination(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameFieldObject dispatcherObject = (MoveRenameFieldObject) dispatcher;
        RenamePackageObject receiverObject = (RenamePackageObject) receiver;

        String dispatcherOriginalClassName = dispatcherObject.getOriginalClass();
        String dispatcherRefactoredClassName = dispatcherObject.getDestinationClass();
        String receiverOriginalPackageName = receiverObject.getOriginalName();
        String receiverDestinationPackageName = receiverObject.getDestinationName();

        // Need to check both cases
        boolean isCombination = false;
        // If the source method's package is renamed after the inline method refactoring
        if(dispatcherOriginalClassName.contains(receiverOriginalPackageName)) {
            String refactoredClassName = dispatcherRefactoredClassName.substring(dispatcherOriginalClassName.lastIndexOf("."));
            // Update the classes package
            ((MoveRenameFieldObject) dispatcher).setOriginalClassName(receiverDestinationPackageName + refactoredClassName);
            isCombination = true;
        }

        // If the extracted method's package is renamed after the inline method refactoring
        if(dispatcherRefactoredClassName.contains(receiverOriginalPackageName)) {
            String refactoredClassName = dispatcherRefactoredClassName.substring(dispatcherRefactoredClassName.lastIndexOf("."));
            // Update the classes package
            ((MoveRenameFieldObject) dispatcher).setDestinationClassName(receiverDestinationPackageName + refactoredClassName);
            isCombination = true;
        }
        return isCombination;
    }
}
