package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;

public class MoveRenameFieldMoveRenameClassCell {

    // Given p1.c1 -> p2.c2 and p2.c2.f1 -> p2.c2.f2
    // p1.c1 -> p2.c2 and p1.c1.f1 -> .2.c2.f2
    // Given p1.c1.f1 -> p1.c1.f2 and p1.c1 -> p2.c2
    // p1.c1.f1 -> p2.c2.f2 and p1.c1 -> p2.c2
    public static boolean checkCombination(RefactoringObject classObject, RefactoringObject fieldObject) {
        MoveRenameClassObject mRCObject = (MoveRenameClassObject) classObject;
        MoveRenameFieldObject fObject = (MoveRenameFieldObject) fieldObject;

        String originalMRClassName = mRCObject.getOriginalClassObject().getClassName();
        String destinationMRClassName = mRCObject.getDestinationClassObject().getClassName();
        String originalFieldClassName = fObject.getOriginalClass();
        String destinationFieldClassName = fObject.getDestinationClass();



        // If the original classes are the same, update the destination class for the field
        if((originalMRClassName.equals(originalFieldClassName) && !destinationFieldClassName.equals(destinationMRClassName))
            || (destinationFieldClassName.equals(originalMRClassName) && !originalFieldClassName.equals(originalMRClassName))) {
            fieldObject.setDestinationFilePath(classObject.getDestinationFilePath());
            ((MoveRenameFieldObject) fieldObject).setDestinationClassName(destinationMRClassName);
        }
        // If the original classes are different but the destination classes are the same, update the original class for the field
        else if(!originalMRClassName.equals(originalFieldClassName) && destinationFieldClassName.equals(destinationMRClassName)) {
            fieldObject.setOriginalFilePath(classObject.getOriginalFilePath());
            ((MoveRenameFieldObject) fieldObject).setOriginalClassName(originalMRClassName);
        }

        return false;
    }

}
