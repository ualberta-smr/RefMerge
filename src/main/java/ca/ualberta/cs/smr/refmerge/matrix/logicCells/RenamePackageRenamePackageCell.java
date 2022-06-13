package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;

public class RenamePackageRenamePackageCell {

    public static boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        // There can only be a naming conflict
        return namingConflict(dispatcher, receiver);
    }

    private static boolean namingConflict(RefactoringObject dispatcher, RefactoringObject receiver) {
        RenamePackageObject dispatcherObject = (RenamePackageObject) dispatcher;
        RenamePackageObject receiverObject = (RenamePackageObject) receiver;

        String dispatcherOriginalName = dispatcherObject.getOriginalName();
        String dispatcherRefactoredName = dispatcherObject.getDestinationName();
        String receiverOriginalName = receiverObject.getOriginalName();
        String receiverRefactoredName = receiverObject.getDestinationName();

        // If the same package was renamed to the same name, then it is the same refactoring
        if(dispatcherOriginalName.equals(receiverOriginalName) && (dispatcherRefactoredName.equals(receiverRefactoredName))) {
            return false;
        }

        // If the same package was renamed to two different package names
        if(dispatcherOriginalName.equals(receiverOriginalName)) {
            return true;
        }
        // Otherwise, if two packages were renamed to the same package
        else return dispatcherRefactoredName.equals(receiverRefactoredName);
    }

    public static boolean checkTransitivity(RefactoringObject firstObject, RefactoringObject secondObject) {
        RenamePackageObject firstPackage = (RenamePackageObject) firstObject;
        RenamePackageObject secondPackage = (RenamePackageObject) secondObject;

        String firstOriginalName = firstPackage.getOriginalName();
        String firstRefactoredName = firstPackage.getDestinationName();
        String secondOriginalName = secondPackage.getOriginalName();
        String secondRefactoredName = secondPackage.getDestinationName();

        // If p1 -> p2 and p2 -> p3
        if(firstRefactoredName.equals(secondOriginalName)) {
            ((RenamePackageObject) firstObject).setDestinationName(secondRefactoredName);
            ((RenamePackageObject) secondObject).setOriginalName(firstOriginalName);
            return true;
        }
        return false;
    }

}
