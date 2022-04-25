package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameFieldObject;
import com.intellij.openapi.project.Project;

public class RenameFieldRenameFieldCell {

    Project project;

    public RenameFieldRenameFieldCell(Project project) {
        this.project = project;
    }

    /*
     * Check if a ename field refactoring conflicts with a rename field refactoring on the other branch.
     * While a field cannot override another field, it can shadow it. Rename Field/Rename Field
     * can result in a naming or shadowing conflict.
     */
    public boolean renameFieldRenameFieldConflictCell(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(project);
        // Check for shadowing conflict

        // Check for naming conflict
        if(cell.checkFieldNamingConflict(dispatcherObject, receiverObject)) {
            return true;
        }
        return false;
    }

    public boolean checkFieldNamingConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        RenameFieldObject dispatcherField = (RenameFieldObject) dispatcherObject;
        RenameFieldObject receiverField = (RenameFieldObject) receiverObject;

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
        return false;
    }
}
