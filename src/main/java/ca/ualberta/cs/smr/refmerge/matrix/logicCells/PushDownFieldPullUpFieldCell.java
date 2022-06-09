package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class PushDownFieldPullUpFieldCell {
    Project project;

    public PushDownFieldPullUpFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) dispatcher;
        PushDownFieldObject pushDownFieldObject = (PushDownFieldObject) receiver;
        // Shadow conflict
        if(shadowConflict(pullUpFieldObject, pushDownFieldObject)) {
            return true;
        }
        // Naming conflict
        return namingConflict(pullUpFieldObject, pushDownFieldObject);
    }

    public boolean shadowConflict(PullUpFieldObject dispatcherObject, PushDownFieldObject receiverObject) {
        String newDispatcherClass = dispatcherObject.getTargetClass();
        String newReceiverClass = receiverObject.getTargetSubClass();

        // Cannot have shadow conflict in same class
        if(newDispatcherClass.equals(newReceiverClass)) {
            return false;
        }

        String dispatcherFile = dispatcherObject.getDestinationFilePath();
        String receiverFile = receiverObject.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, newDispatcherClass);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, newReceiverClass);
        if(psiReceiver != null && psiDispatcher != null) {
            // If there is no inheritance relationship, there is no shadow conflict
            if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }

        String newDispatcherField = dispatcherObject.getRefactoredFieldName();
        String newReceiverField = receiverObject.getRefactoredFieldName();

        // The original name does not matter in this case
        return isSameName(newDispatcherField, newReceiverField);

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
