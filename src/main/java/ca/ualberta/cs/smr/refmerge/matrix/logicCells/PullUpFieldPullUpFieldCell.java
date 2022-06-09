package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class PullUpFieldPullUpFieldCell {

    Project project;

    public PullUpFieldPullUpFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PullUpFieldObject dispatcherObject = (PullUpFieldObject) dispatcher;
        PullUpFieldObject receiverObject = (PullUpFieldObject) receiver;
        // Accidental shadow conflict
        if(shadowConflict(dispatcherObject, receiverObject)) {
            return  true;
        }
        // Naming conflict
        return namingConflict(dispatcherObject, receiverObject);
    }

    public boolean shadowConflict(PullUpFieldObject dispatcherObject, PullUpFieldObject receiverObject) {

        String newDispatcherClass = dispatcherObject.getTargetClass();
        String newReceiverClass = receiverObject.getTargetClass();

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

    public boolean namingConflict(PullUpFieldObject dispatcher, PullUpFieldObject receiver) {

        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String receiverOriginalClass = receiver.getOriginalClass();

        String dispatcherTargetClass = dispatcher.getTargetClass();
        String receiverTargetClass = receiver.getTargetClass();

        // If the fields are not pulled up from the same class, it cannot result in a naming conflict
        // because fields pulled up from different classes that are the same field have potential to be combined
        if(!dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return false;
        }

        String dispatcherOriginalFieldName = dispatcher.getOriginalFieldName();
        String dispatcherDestinationFieldName = dispatcher.getRefactoredFieldName();
        String receiverOriginalFieldName = receiver.getOriginalFieldName();
        String receiverDestinationFieldName = receiver.getRefactoredFieldName();

        // If the original field names are not the same and the new field names are not the same, there is no potential
        // for a naming conflict
        if(!dispatcherOriginalFieldName.equals(receiverOriginalFieldName)
                && !dispatcherDestinationFieldName.equals(receiverDestinationFieldName)) {
            return false;
        }

        // If the same field is pulled up from the same source class to the same target class
        // it is the same refactoring and is not conflicting
        if(dispatcherOriginalFieldName.equals(receiverOriginalFieldName)
                && dispatcherDestinationFieldName.equals(receiverDestinationFieldName)
                && dispatcherTargetClass.equals(receiverTargetClass)) {
            return false;
        }

        // If the same field is pulled up to two different classes, report a naming conflict
        return dispatcherOriginalFieldName.equals(receiverOriginalFieldName)
                && !dispatcherTargetClass.equals(receiverTargetClass);
    }

}
