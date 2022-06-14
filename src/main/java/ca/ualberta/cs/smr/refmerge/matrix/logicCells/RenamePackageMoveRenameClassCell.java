package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ClassObject;

public class RenamePackageMoveRenameClassCell {

    public static void checkCombination(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameClassObject dispatcherObject = (MoveRenameClassObject) dispatcher;
        RenamePackageObject receiverObject = (RenamePackageObject) receiver;

        ClassObject classObject = dispatcherObject.getDestinationClassObject();
        String dispatcherDestinationPackageName = classObject.getPackageName();


        String receiverOriginalPackageName = receiverObject.getOriginalName();
        String receiverDestinationPackageName = receiverObject.getDestinationName();

        // If p1.c1 -> p1.c2 before p1.c2 -> p2.c2
        if(dispatcherDestinationPackageName.contains(receiverOriginalPackageName)) {
            classObject.setPackageName(receiverDestinationPackageName);
            // Update the destination package
            ((MoveRenameClassObject) dispatcher).setDestinationClassObject(classObject);
        }
    }

}
