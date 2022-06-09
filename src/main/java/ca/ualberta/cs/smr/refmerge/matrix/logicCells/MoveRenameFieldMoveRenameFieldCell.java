package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class MoveRenameFieldMoveRenameFieldCell {

    Project project;

    public MoveRenameFieldMoveRenameFieldCell(Project project) {
        this.project = project;
    }

    /*
     * Check if a rename + move field refactoring conflicts with a rename + move field refactoring on the other branch.
     * While a field cannot override another field, it can shadow it. Rename+Move Field/Rename+Move Field
     * can result in a naming or shadowing conflict.
     */
    public boolean renameFieldRenameFieldConflictCell(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(project);
        // Check for shadowing conflict
        if(cell.checkShadowConflict(dispatcherObject, receiverObject)) {
            System.out.println("Shadow Conflict");
            return true;
        }
        // Check for naming conflict
        else if(cell.checkFieldNamingConflict(dispatcherObject, receiverObject)) {
            System.out.println("Naming Conflict");
            return true;
        }
        return false;
    }

    public boolean checkShadowConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameFieldObject dispatcherField = (MoveRenameFieldObject)  dispatcherObject;
        MoveRenameFieldObject receiverField = (MoveRenameFieldObject) receiverObject;

        String newDispatcherClass = dispatcherField.getDestinationClass();
        String newReceiverClass = receiverField.getDestinationClass();

        // Cannot have shadow conflict in same class
        if(newDispatcherClass.equals(newReceiverClass)) {
            return false;
        }

        String dispatcherFile = dispatcherField.getDestinationFilePath();
        String receiverFile = receiverField.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, newDispatcherClass);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, newReceiverClass);
        if(psiReceiver != null && psiDispatcher != null) {
            // If there is no inheritance relationship, there is no shadow conflict
            if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }

        String originalDispatcherField = dispatcherField.getOriginalName();
        String originalReceiverField = receiverField.getOriginalName();
        String newDispatcherField = dispatcherField.getDestinationName();
        String newReceiverField = receiverField.getDestinationName();

        return !isSameName(originalDispatcherField, originalReceiverField) &&
                isSameName(newDispatcherField, newReceiverField);

    }

    public boolean checkFieldNamingConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameFieldObject dispatcherField = (MoveRenameFieldObject) dispatcherObject;
        MoveRenameFieldObject receiverField = (MoveRenameFieldObject) receiverObject;

        // Get the field names
        String originalDispatcherField = dispatcherField.getOriginalName();
        String originalReceiverField = receiverField.getOriginalName();
        String newDispatcherField = dispatcherField.getDestinationName();
        String newReceiverField = receiverField.getDestinationName();

        // Get the relevant class names
        String originalDispatcherClass = dispatcherField.getOriginalClass();
        String newDispatcherClass = dispatcherField.getDestinationClass();
        String originalReceiverClass = receiverField.getOriginalClass();
        String newReceiverClass = receiverField.getDestinationClass();

        // If both refactorings are rename field refactorings
        // The logic in rename field and move field conflict checks encapsulate the logic in move & rename conflict
        if(dispatcherField.isRename() && receiverField.isRename()) {
            // Check that the rename refactorings occur in the same class
            if(originalDispatcherClass.equals(originalReceiverClass)) {
                // If the fields have the same starting name but different refactored names, it is conflicting
                if(originalDispatcherField.equals(originalReceiverField) && !newDispatcherField.equals(newReceiverField)) {
                    return true;
                }
                // Otherwise, if two fields are renamed to the same name in the same class, it is conflicting
                else if(!originalDispatcherField.equals(originalReceiverField) && newDispatcherField.equals(newReceiverField)) {
                    return true;
                }
            }
        }
        // If they are both move field
        else if(dispatcherField.isMove() && receiverField.isMove()) {
            if(originalDispatcherClass.equals(originalReceiverClass)) {
                // If it is the same original field in the same original class and the new classes differ
                if(originalDispatcherField.equals(originalReceiverField) && !newDispatcherClass.equals(newReceiverClass)) {
                    return true;
                }
            }
            // Otherwise if the fields originate from different classes but are moved to the same class
            else {
                if(originalDispatcherField.equals(originalReceiverField) && newDispatcherClass.equals(newReceiverClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkDependence(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        MoveRenameFieldObject firstObject = (MoveRenameFieldObject) firstRefactoring;
        MoveRenameFieldObject secondObject = (MoveRenameFieldObject) secondRefactoring;

        String firstOriginalClass = firstObject.getOriginalClass();
        String firstOriginalField = firstObject.getOriginalName();
        String secondOriginalClass = secondObject.getOriginalClass();
        String secondOriginalField = secondObject.getOriginalName();

        // If the same field is not being moved and renamed, then there cannot be dependence
        if(!(firstOriginalClass.equals(secondOriginalClass) && firstOriginalField.equals(secondOriginalField))) {
            return false;
        }
        // If both refactorings are rename, then there is no dependence
        if(firstObject.isRename() && secondObject.isRename()) {
            return false;
        }
        // If both refactorings are move related refactorings, there is no dependence
        if(firstObject.isMove() && secondObject.isMove()) {
            return false;
        }
        // One refactoring must be move and the other must be rename at this stage
        return true;
    }

    public boolean checkTransitivity(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        MoveRenameFieldObject firstObject = (MoveRenameFieldObject) firstRefactoring;
        MoveRenameFieldObject secondObject = (MoveRenameFieldObject) secondRefactoring;

        // Get field information
        String newFirstName = firstObject.getDestinationName();
        String oldSecondName = secondObject.getOriginalName();
        String newSecondName = secondObject.getDestinationName();

        // Get class information
        String newFirstClass = firstObject.getDestinationClass();
        String oldSecondClass = secondObject.getOriginalClass();
        String newSecondClass = secondObject.getDestinationClass();

        // If c2 == c3 and f2 == f3 where c1.f1 -> c2.f2 and c3.f3 -> c4.f4, then they can be combined to c1.f1 -> c4.f4
        if(newFirstClass.equals(oldSecondClass) && newFirstName.equals(oldSecondName)) {
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((MoveRenameFieldObject) firstRefactoring).setDestinationClassName(newSecondClass);
            ((MoveRenameFieldObject) firstRefactoring).setDestinationFieldName(newSecondName);
            return true;
        }




        return false;
    }
}
