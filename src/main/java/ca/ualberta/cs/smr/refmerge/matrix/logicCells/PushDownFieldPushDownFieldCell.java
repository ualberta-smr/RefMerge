package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class PushDownFieldPushDownFieldCell {
    Project project;

    public PushDownFieldPushDownFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PushDownFieldObject dispatcherObject = (PushDownFieldObject) dispatcher;
        PushDownFieldObject receiverObject = (PushDownFieldObject) receiver;
        // Shadow conflict
        // Naming conflict
        return namingConflict(dispatcherObject, receiverObject);
    }

    public boolean namingConflict(PushDownFieldObject dispatcher, PushDownFieldObject receiver) {

        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String receiverOriginalClass = receiver.getOriginalClass();

        String dispatcherOriginalFieldName = dispatcher.getOriginalFieldName();
        String dispatcherDestinationFieldName = dispatcher.getRefactoredFieldName();
        String receiverOriginalFieldName = receiver.getOriginalFieldName();
        String receiverDestinationFieldName = receiver.getRefactoredFieldName();

        // If the fields are not pushed down from the same class, it cannot result in a naming conflict
        // because fields pushed down from the same class that are the same field have potential to be combined
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalFieldName.equals(receiverOriginalFieldName)) {
            return false;
        }


        // If the original field names are not the same and the new field names are not the same, there is no potential
        // for a naming conflict
        if(!dispatcherOriginalFieldName.equals(receiverOriginalFieldName)
                && !dispatcherDestinationFieldName.equals(receiverDestinationFieldName)) {
            return false;
        }

        // If the two fields are pushed down from two different classes, report a naming conflict
        return dispatcherOriginalFieldName.equals(receiverOriginalFieldName)
                && !dispatcherOriginalClass.equals(receiverOriginalClass);
    }

}
