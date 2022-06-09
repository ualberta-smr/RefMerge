package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class PushDownFieldMoveRenameFieldCell {
    Project project;

    public PushDownFieldMoveRenameFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameFieldObject moveRenameFieldObject = (MoveRenameFieldObject) dispatcher;
        PushDownFieldObject pushDownFieldObject = (PushDownFieldObject) receiver;

        // Shadow conflict
        if(shadowConflict(moveRenameFieldObject, pushDownFieldObject)) {
            return true;
        }
        // Naming conflict
        return namingConflict(moveRenameFieldObject, pushDownFieldObject);
    }

    public boolean shadowConflict(MoveRenameFieldObject dispatcher, PushDownFieldObject receiver) {

        String newDispatcherClass = dispatcher.getDestinationClass();
        String newReceiverClass = receiver.getTargetSubClass();

        // Cannot have shadow conflict in same class
        if(newDispatcherClass.equals(newReceiverClass)) {
            return false;
        }

        String dispatcherFile = dispatcher.getDestinationFilePath();
        String receiverFile = receiver.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, newDispatcherClass);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, newReceiverClass);
        if(psiReceiver != null && psiDispatcher != null) {
            // If there is no inheritance relationship, there is no shadow conflict
            if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }

        String newDispatcherField = dispatcher.getDestinationName();
        String newReceiverField = receiver.getRefactoredFieldName();

        // The original name does not matter in this case
        return isSameName(newDispatcherField, newReceiverField);


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
