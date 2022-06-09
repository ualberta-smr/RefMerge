package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
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

    public void checkCombination(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameFieldObject dispatcher = (MoveRenameFieldObject) dispatcherObject;
        PushDownFieldObject receiver = (PushDownFieldObject) receiverObject;

        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String dispatcherDestinationClass = dispatcher.getDestinationClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetSubClass();

        String dispatcherOriginalField = dispatcher.getOriginalName();
        String dispatcherDestinationField = dispatcher.getDestinationName();
        String receiverOriginalField = receiver.getOriginalFieldName();
        String receiverDestinationField = receiver.getRefactoredFieldName();

        // If the move+rename field refactoring happens before the push down field refactoring,
        // The refactored name+class will equal the push down field's original name+class
        if(dispatcherDestinationClass.equals(receiverOriginalClass)
                && dispatcherDestinationField.equals(receiverOriginalField)) {
            // Update the original field for push down field to check for conflicts
            ((PushDownFieldObject) receiverObject).setOriginalFieldName(dispatcherOriginalField);
            // Update the original class for push down field to check for conflicts
            receiverObject.setOriginalFilePath(dispatcherObject.getOriginalFilePath());
            ((PushDownFieldObject) receiverObject).setOriginalClass(dispatcherOriginalClass);
            // Update the refactored field and class name for move+rename field so we can find future
            // refactorings that might change the same program element
            ((MoveRenameFieldObject) dispatcherObject).setDestinationFieldName(receiverDestinationField);
            dispatcherObject.setDestinationFilePath(receiverObject.getDestinationFilePath());
            ((MoveRenameFieldObject) dispatcherObject).setDestinationClassName(receiverDestinationClass);



        }


        // If the push down field happens before the move+rename field, the refactored push down field's name+class
        // will equal the move+rename field's original name+class
        if(dispatcherOriginalClass.equals(receiverDestinationClass)
                && dispatcherOriginalField.equals(receiverDestinationField)) {
            // Update the destination field and class for the push down field refactoring
            ((PushDownFieldObject) receiverObject).setRefactoredFieldName(dispatcherDestinationField);
            receiverObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
            ((PushDownFieldObject) receiverObject).setTargetSubClass(dispatcherDestinationClass);
            // Update the original field and class for the move+rename field refactoring
            ((MoveRenameFieldObject) dispatcherObject).setOriginalFieldName(receiverOriginalField);
            dispatcherObject.setOriginalFilePath(receiverObject.getOriginalFilePath());
            ((MoveRenameFieldObject) dispatcherObject).setOriginalClassName(receiverOriginalClass);

        }


    }

}
