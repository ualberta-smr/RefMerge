package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;

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
        if(overloadConflict(extractMethodObject, pullUpMethodObject)) {
            return true;
        }
        // Check for naming conflict
        return namingConflict(extractMethodObject, pullUpMethodObject);
    }

    public  boolean overloadConflict(ExtractMethodObject dispatcher, PullUpMethodObject receiver) {
        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();
        // Get class names
        String dispatcherClassName = dispatcher.getOriginalClassName();
        String receiverClassName = receiver.getOriginalClass();
        // If the methods are in different classes, no overloading happens
        if (!dispatcherClassName.equals(receiverClassName)) {
            Utils utils = new Utils(project);
            String dispatcherFile = dispatcher.getOriginalFilePath();
            String receiverFile = receiver.getOriginalFilePath();
            PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
            PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
            if(psiReceiver != null && psiDispatcher != null) {
                if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                    return false;
                }
            }
        }
        String dispatcherDestinationMethodName = dispatcherDestinationMethod.getName();
        String receiverDestinationMethodName = receiverDestinationMethod.getName();
        // If two methods with different signatures are refactored to the same method name, this overloading conflict
        return (dispatcherDestinationMethodName.equals(receiverDestinationMethodName)) &&
                !dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
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
