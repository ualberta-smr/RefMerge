package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PullUpMethodMoveRenameMethodCell {
    Project project;

    public PullUpMethodMoveRenameMethodCell(Project project) {
        this.project = project;
    }

    /*
     * Check for naming conflict, override conflict, and overload conflict between pull up method and move+rename method
     * refactorings.
     */
    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameMethodObject dispatcherObject = (MoveRenameMethodObject) dispatcher;
        PullUpMethodObject receiverObject = (PullUpMethodObject) receiver;

        // Check for override conflict

        // Check for overload conflict

        // Check for naming conflict
        if(namingConflict(dispatcherObject, receiverObject)) {
            return true;
        }

        return false;
    }

    public boolean namingConflict(MoveRenameMethodObject dispatcher, PullUpMethodObject receiver) {
        String dispatcherOriginalClass = dispatcher.getOriginalClassName();
        String dispatcherDestinationClass = dispatcher.getDestinationClassName();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();

        // If the same method is moved or renamed and pulled up, it is conflicting
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return true;
        }

        // If two methods are pulled up to and moved or renamed to the same method, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }


}
