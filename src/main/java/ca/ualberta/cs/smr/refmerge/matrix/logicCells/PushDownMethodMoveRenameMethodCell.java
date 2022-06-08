package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PushDownMethodMoveRenameMethodCell {

    Project project;

    public PushDownMethodMoveRenameMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) dispatcher;
        PushDownMethodObject pushDownMethodObject = (PushDownMethodObject) receiver;
        // Override conflict
        // Overload conflict
        // Naming conflict
        return namingConflict(moveRenameMethodObject, pushDownMethodObject);
    }

    public boolean namingConflict(MoveRenameMethodObject dispatcher, PushDownMethodObject receiver) {
        String dispatcherOriginalClass = dispatcher.getOriginalClassName();
        String dispatcherDestinationClass = dispatcher.getDestinationClassName();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetBaseClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();

        // If the same method is moved or renamed and pushed down, it is conflicting
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return true;
        }

        // If two methods are pushed down and moved or renamed to the same method, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }

}
