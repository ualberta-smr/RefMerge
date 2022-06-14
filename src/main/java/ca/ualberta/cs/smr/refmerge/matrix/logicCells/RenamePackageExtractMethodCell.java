package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;

public class RenamePackageExtractMethodCell {

    public static void checkCombination(RefactoringObject dispatcher, RefactoringObject receiver) {
        ExtractMethodObject dispatcherObject = (ExtractMethodObject) dispatcher;
        RenamePackageObject receiverObject = (RenamePackageObject) receiver;

        String dispatcherOriginalClassName = dispatcherObject.getOriginalClassName();
        String dispatcherRefactoredClassName = dispatcherObject.getDestinationClassName();
        String receiverOriginalPackageName = receiverObject.getOriginalName();
        String receiverDestinationPackageName = receiverObject.getDestinationName();

        // Need to check both cases
        // If the source method's package is renamed after the extract method refactoring
        if(dispatcherOriginalClassName.contains(receiverOriginalPackageName)) {
            String refactoredClassName = dispatcherRefactoredClassName.substring(dispatcherOriginalClassName.lastIndexOf("."));
            // Update the classes package
            ((ExtractMethodObject) dispatcher).setOriginalClassName(receiverDestinationPackageName + refactoredClassName);
        }

        // If the extracted method's package is renamed after the extract method refactoring
        if(dispatcherRefactoredClassName.contains(receiverOriginalPackageName)) {
            String refactoredClassName = dispatcherRefactoredClassName.substring(dispatcherRefactoredClassName.lastIndexOf("."));
            // Update the classes package
            ((ExtractMethodObject) dispatcher).setDestinationClassName(receiverDestinationPackageName + refactoredClassName);
        }
    }

}
