package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PullUpMethodExtractMethodCell {
    Project project;

    public PullUpMethodExtractMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) dispatcher;
        PullUpMethodObject pullUpMethodObject = (PullUpMethodObject) receiver;
        // Check for override conflict
        // Check for overload conflict
        // Check for naming conflict
        if(namingConflict(extractMethodObject, pullUpMethodObject)) {
            return true;
        }
        return false;
    }

    public boolean namingConflict(ExtractMethodObject dispatcher, PullUpMethodObject receiver) {
        String dispatcherDestinationClass = dispatcher.getDestinationClassName();
        String receiverDestinationClass = receiver.getTargetClass();

        // Only need to get the destination signatures because the extract method refactoring creates a new
        // program element. The original signature for extract method is the signature the extract method
        // refactoring comes from
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();


        // If two methods are pulled up to and extracted to the same method, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }
}
