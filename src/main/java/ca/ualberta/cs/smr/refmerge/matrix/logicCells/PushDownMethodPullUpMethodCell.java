package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PushDownMethodPullUpMethodCell {

    Project project;

    public PushDownMethodPullUpMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PullUpMethodObject pullUpMethodObject = (PullUpMethodObject) dispatcher;
        PushDownMethodObject pushDownMethodObject = (PushDownMethodObject) receiver;

        // Check Naming Conflict
        return namingConflict(pullUpMethodObject, pushDownMethodObject);
    }

    public boolean namingConflict(PullUpMethodObject dispatcher, PushDownMethodObject receiver) {

        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String receiverOriginalClass = receiver.getOriginalClass();
        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();

        String dispatcherDestinationClass = dispatcher.getTargetClass();
        String receiverDestinationClass = receiver.getTargetBaseClass();
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();

        // If the same method is pulled up on one branch and pushed down on the other, this is conflicting
        if(dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod) && dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return true;
        }

        // If two methods are pushed down and pulled up to the same location with the same signature, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);

    }

}
