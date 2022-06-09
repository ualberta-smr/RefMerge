package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class PushDownMethodMoveRenameClassCell {

    Project project;

    public PushDownMethodMoveRenameClassCell(Project project) {
        this.project = project;
    }

    public void checkCombination(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameClassObject dispatcher = (MoveRenameClassObject) dispatcherObject;
        PushDownMethodObject receiver = (PushDownMethodObject) receiverObject;

        String dispatcherOriginalClass = dispatcher.getOriginalClassObject().getClassName();
        String receiverOriginalClass = receiver.getOriginalClass();
        String dispatcherOriginalFile = dispatcher.getOriginalFilePath();
        String receiverOriginalFile = receiver.getOriginalFilePath();

        String dispatcherDestinationClass = dispatcher.getDestinationClassObject().getClassName();
        String receiverDestinationClass = receiver.getTargetBaseClass();
        String dispatcherDestinationFile = dispatcher.getDestinationFilePath();
        String receiverDestinationFile = receiver.getDestinationFilePath();

        // If the push down method refactoring happens after the class refactoring, update the corresponding method's location
        if(dispatcherDestinationClass.equals(receiverOriginalClass) && dispatcherDestinationFile.equals(receiverOriginalFile)) {
            receiverObject.setOriginalFilePath(dispatcherOriginalFile);
            ((PushDownMethodObject) receiverObject).setOriginalClass(dispatcherOriginalClass);
        }

        if(dispatcherDestinationClass.equals(receiverDestinationClass) && dispatcherDestinationFile.equals(receiverDestinationFile)) {
            receiverObject.setDestinationFilePath(dispatcherOriginalFile);
            ((PushDownMethodObject) receiverObject).setTargetBaseClass(dispatcherOriginalClass);
        }



    }

}
