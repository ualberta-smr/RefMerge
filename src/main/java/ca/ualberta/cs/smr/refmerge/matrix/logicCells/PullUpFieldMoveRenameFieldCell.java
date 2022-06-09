package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class PullUpFieldMoveRenameFieldCell {
    Project project;

    public PullUpFieldMoveRenameFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameFieldObject moveRenameFieldObject = (MoveRenameFieldObject) dispatcher;
        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) receiver;
        // Shadow conflict
        // Naming conflict
        return namingConflict(moveRenameFieldObject, pullUpFieldObject);
    }

    public boolean namingConflict(MoveRenameFieldObject dispatcher, PullUpFieldObject receiver) {
        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String dispatcherDestinationClass = dispatcher.getDestinationClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetClass();

        String dispatcherOriginalFieldName = dispatcher.getOriginalName();
        String dispatcherDestinationFieldName = dispatcher.getDestinationName();
        String receiverOriginalFieldName = receiver.getOriginalFieldName();
        String receiverDestinationFieldName = receiver.getRefactoredFieldName();

        // If the same field is moved or renamed and pulled up, a naming conflict happened
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalFieldName.equals(receiverOriginalFieldName)) {
            return true;
        }
        // If two fields are pulled up to and moved or renamed to the same method, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationFieldName.equals(receiverDestinationFieldName);
    }
}
