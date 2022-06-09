package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class PullUpFieldMoveRenameFieldCell {
    Project project;

    public PullUpFieldMoveRenameFieldCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameFieldObject moveRenameFieldObject = (MoveRenameFieldObject) dispatcher;
        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) receiver;
        // Shadow conflict
        if(shadowConflict(moveRenameFieldObject, pullUpFieldObject)) {
            return true;
        }
        // Naming conflict
        return namingConflict(moveRenameFieldObject, pullUpFieldObject);
    }

    public boolean shadowConflict(MoveRenameFieldObject dispatcher, PullUpFieldObject receiver) {

        String newDispatcherClass = dispatcher.getDestinationClass();
        String newReceiverClass = receiver.getTargetClass();

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

    public void checkCombination(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameFieldObject dispatcher = (MoveRenameFieldObject) dispatcherObject;
        PullUpFieldObject receiver = (PullUpFieldObject) receiverObject;
        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String dispatcherDestinationClass = dispatcher.getDestinationClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetClass();

        String dispatcherOriginalFieldName = dispatcher.getOriginalName();
        String dispatcherDestinationFieldName = dispatcher.getDestinationName();
        String receiverOriginalFieldName = receiver.getOriginalFieldName();
        String receiverDestinationFieldName = receiver.getRefactoredFieldName();

        // If the move+rename field refactoring happens before the pull up field refactoring,
        // The refactored name+class will equal the pull up field's original name+class
        if(dispatcherDestinationClass.equals(receiverOriginalClass)
                && dispatcherDestinationFieldName.equals(receiverOriginalFieldName)) {
            // Update the original field name for pull up field to check for conflicts
            ((PullUpFieldObject) receiverObject).setOriginalFieldName(dispatcherOriginalFieldName);
            // Update the original class for pull up field to check for conflicts
            receiverObject.setOriginalFilePath(dispatcherObject.getOriginalFilePath());
            ((PullUpFieldObject) receiverObject).setOriginalClass(dispatcherOriginalClass);
            // Update the refactored field signature and class name for move+rename field so we can find future
            // refactorings that might change the same program element
            ((MoveRenameFieldObject) dispatcherObject).setDestinationFieldName(receiverDestinationFieldName);
            dispatcherObject.setDestinationFilePath(receiverObject.getDestinationFilePath());
            ((MoveRenameFieldObject) dispatcherObject).setDestinationClassName(receiverDestinationClass);



        }


        // If the pull up field happens before the move+rename field, the refactored pull up field's name+class
        // will equal the move+rename field's original name+class
        if(dispatcherOriginalClass.equals(receiverDestinationClass)
                && dispatcherOriginalFieldName.equals(receiverDestinationFieldName)) {
            // Update the destination field and class for the pull up field refactoring
            ((PullUpFieldObject) receiverObject).setRefactoredFieldName(dispatcherDestinationFieldName);
            receiverObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
            ((PullUpFieldObject) receiverObject).setTargetClass(dispatcherDestinationClass);
            // Update the original field and class for the move+rename field refactoring
            ((MoveRenameFieldObject) dispatcherObject).setOriginalFieldName(receiverOriginalFieldName);
            dispatcherObject.setOriginalFilePath(receiverObject.getOriginalFilePath());
            ((MoveRenameFieldObject) dispatcherObject).setOriginalClassName(receiverOriginalClass);

        }



    }
}
