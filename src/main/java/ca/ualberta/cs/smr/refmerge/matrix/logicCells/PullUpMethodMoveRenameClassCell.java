package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class PullUpMethodMoveRenameClassCell {
    Project project;

    public PullUpMethodMoveRenameClassCell(Project project) {
        this.project = project;
    }

    public void checkCombination(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameClassObject dispatcher = (MoveRenameClassObject) dispatcherObject;
        PullUpMethodObject receiver = (PullUpMethodObject) receiverObject;

        String dispatcherOriginalClass = dispatcher.getOriginalClassObject().getClassName();
        String receiverOriginalClass = receiver.getOriginalClass();
        String dispatcherOriginalFile = dispatcher.getOriginalFilePath();
        String receiverOriginalFile = receiver.getOriginalFilePath();

        String dispatcherDestinationClass = dispatcher.getDestinationClassObject().getClassName();
        String receiverDestinationClass = receiver.getTargetClass();
        String dispatcherDestinationFile = dispatcher.getDestinationFilePath();
        String receiverDestinationFile = receiver.getDestinationFilePath();

        // If the pull up method refactoring happens after the class refactoring, update the corresponding method's location
        if(dispatcherDestinationClass.equals(receiverOriginalClass) && dispatcherDestinationFile.equals(receiverOriginalFile)) {
            receiverObject.setOriginalFilePath(dispatcherOriginalFile);
            ((PullUpMethodObject) receiverObject).setOriginalClass(dispatcherOriginalClass);
        }

        if(dispatcherDestinationClass.equals(receiverDestinationClass) && dispatcherDestinationFile.equals(receiverDestinationFile)) {
            receiverObject.setDestinationFilePath(dispatcherOriginalFile);
            ((PullUpMethodObject) receiverObject).setTargetClass(dispatcherOriginalClass);
        }



    }

}
