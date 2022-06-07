package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PushDownMethodPushDownMethodCell {

    Project project;

    public PushDownMethodPushDownMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PushDownMethodObject dispatcherObject = (PushDownMethodObject) dispatcher;
        PushDownMethodObject receiverObject = (PushDownMethodObject) receiver;
        // Override conflict

        // Overload conflict

        // Naming Conflict
        return namingConflict(dispatcherObject, receiverObject);

    }

    private boolean namingConflict(PushDownMethodObject dispatcherObject, PushDownMethodObject receiverObject) {

        String dispatcherOriginalClass = dispatcherObject.getOriginalClass();
        String receiverOriginalClass = receiverObject.getOriginalClass();

        String dispatcherTargetClass = dispatcherObject.getTargetBaseClass();
        String receiverTargetClass = receiverObject.getTargetBaseClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcherObject.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverObject.getOriginalMethodSignature();
        MethodSignatureObject dispatcherRefactoredMethod = dispatcherObject.getDestinationMethodSignature();
        MethodSignatureObject receiverRefactoredMethod = receiverObject.getDestinationMethodSignature();

        // If the methods are the same and pushed down from the same class, it cannot result in a naming conflict
        // because methods pushed down from the same class can be combined
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return false;
        }

        // if the original method signature is not the same, it cannot be a naming conflict in this case
        if(!dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return false;
        }

        // If the same method is pushed down from the same source class to the same target class
        // it is the same refactoring and is not conflicting
        if(dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)
                && dispatcherRefactoredMethod.equalsSignature(receiverRefactoredMethod)
                && dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return false;
        }

        // If the same method is pushed down from two different classes, report a naming conflict
        return dispatcherRefactoredMethod.equalsSignature(receiverRefactoredMethod)
                && dispatcherTargetClass.equals(receiverTargetClass);
    }

}
