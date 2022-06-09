package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class PushDownFieldMoveRenameFieldCell {
    Project project;

    public PushDownFieldMoveRenameFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameFieldObject moveRenameFieldObject = (MoveRenameFieldObject) dispatcher;
        PushDownFieldObject pushDownFieldObject = (PushDownFieldObject) receiver;

        // Shadow conflict

        // Naming conflict
        return namingConflict(moveRenameFieldObject, pushDownFieldObject);
    }

    public boolean namingConflict(MoveRenameFieldObject dispatcher, PushDownFieldObject receiver) {
        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String dispatcherDestinationClass = dispatcher.getDestinationClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetSubClass();

        String dispatcherOriginalFieldName = dispatcher.getOriginalName();
        String dispatcherDestinationFieldName = dispatcher.getDestinationName();
        String receiverOriginalFieldName = receiver.getOriginalFieldName();
        String receiverDestinationFieldName = receiver.getRefactoredFieldName();

        // If the same field is moved or renamed and pushed down, a naming conflict happened
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalFieldName.equals(receiverOriginalFieldName)) {
            return true;
        }
        // If two fields are pushed down to and moved or renamed to the same field, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationFieldName.equals(receiverDestinationFieldName);
    }

}
