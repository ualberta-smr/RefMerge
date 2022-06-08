package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;

public class PushDownMethodExtractMethodCell {

    Project project;

    public PushDownMethodExtractMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) dispatcher;
        PushDownMethodObject pushDownMethodObject = (PushDownMethodObject) receiver;
        // Override conflict

        // Overload conflict
        if(overloadConflict(extractMethodObject, pushDownMethodObject)) {
            return true;
        }
        // Naming conflict
        return namingConflict(extractMethodObject, pushDownMethodObject);
    }

    public  boolean overloadConflict(ExtractMethodObject dispatcher, PushDownMethodObject receiver) {
        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();
        // Get class names
        String dispatcherClassName = dispatcher.getDestinationClassName();
        String receiverClassName = receiver.getTargetBaseClass();
        // If the methods are in different classes, no overloading happens
        if (!dispatcherClassName.equals(receiverClassName)) {
            Utils utils = new Utils(project);
            String dispatcherFile = dispatcher.getDestinationFilePath();
            String receiverFile = receiver.getDestinationFilePath();
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

    public boolean namingConflict(ExtractMethodObject dispatcher, PushDownMethodObject receiver) {
        String dispatcherDestinationClass = dispatcher.getDestinationClassName();
        String receiverDestinationClass = receiver.getTargetBaseClass();

        // Only need to get the destination signatures because the extract method refactoring creates a new
        // program element. The original signature for extract method is the signature the extract method
        // refactoring comes from
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();


        // If two methods are pushed down to and extracted to the same method, it is conflicting
        return dispatcherDestinationClass.equals(receiverDestinationClass)
                && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }


}
