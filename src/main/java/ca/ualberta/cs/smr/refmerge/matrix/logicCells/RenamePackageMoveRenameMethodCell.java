package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;

public class RenamePackageMoveRenameMethodCell {

    public static void checkCombination(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameMethodObject dispatcherObject = (MoveRenameMethodObject) dispatcher;
        RenamePackageObject receiverObject = (RenamePackageObject) receiver;

        String dispatcherRefactoredClassName = dispatcherObject.getDestinationClassName();
        String receiverOriginalPackageName = receiverObject.getOriginalName();
        String receiverDestinationPackageName = receiverObject.getDestinationName();

        // If c1 -> c2 before p1.c2 -> p2.c2
        if(dispatcherRefactoredClassName.contains(receiverOriginalPackageName)) {
            String refactoredClassName = dispatcherRefactoredClassName.substring(dispatcherRefactoredClassName.lastIndexOf("."));
            // Update the classes package
            ((MoveRenameMethodObject) dispatcher).setDestinationClassName(receiverDestinationPackageName + refactoredClassName);
        }
    }

}
