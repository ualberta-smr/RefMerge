package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import com.intellij.openapi.project.Project;

public class PushDownFieldPullUpFieldCell {
    Project project;

    public PushDownFieldPullUpFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) dispatcher;
        PushDownFieldObject pushDownFieldObject = (PushDownFieldObject) receiver;
        // Shadow conflict

        // Naming conflict
        return namingConflict(pullUpFieldObject, pushDownFieldObject);
    }

    public boolean namingConflict(PullUpFieldObject dispatcher, PushDownFieldObject receiver) {

        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        String dispatcherOriginalField = dispatcher.getOriginalFieldName();
        String receiverOriginalField = receiver.getOriginalFieldName();

        String dispatcherDestinationClass = dispatcher.getTargetClass();
        String receiverDestinationClass = receiver.getTargetSubClass();
        String dispatcherDestinationField = dispatcher.getRefactoredFieldName();
        String receiverDestinationField = receiver.getRefactoredFieldName();

        // If the same field is pulled up on one branch and pushed down on the other, this is conflicting
        if(dispatcherOriginalField.equals(receiverOriginalField) && dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return true;
        }

        // If two fields are pushed down and pulled up to the same location with the same signature, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationField.equals(receiverDestinationField);

    }

}
