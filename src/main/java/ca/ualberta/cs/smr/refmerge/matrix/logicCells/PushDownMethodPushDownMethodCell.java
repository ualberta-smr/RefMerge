package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

import java.util.List;

public class PushDownMethodPushDownMethodCell {

    Project project;

    public PushDownMethodPushDownMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        PushDownMethodObject dispatcherObject = (PushDownMethodObject) dispatcher;
        PushDownMethodObject receiverObject = (PushDownMethodObject) receiver;
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

    public boolean checkTransitivity(RefactoringObject receiverObject, RefactoringObject dispatcherObject) {
        PushDownMethodObject receiver = (PushDownMethodObject) receiverObject;
        PushDownMethodObject dispatcher = (PushDownMethodObject) dispatcherObject;


        String dispatcherOriginalClass = dispatcher.getOriginalClass();
        String receiverOriginalClass = receiver.getOriginalClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();


        // If the two push down method refactorings are from different super classes, there is no transitivity
        if(!dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return false;
        }

        // If the original class is the same and the method signatures are the same, then it is transitive
        // and we need to combine their subclass lists
        if(dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            List<Pair<String, String>> dispatcherSubClasses = dispatcher.getSubClasses();
            List<Pair<String, String>> receiverSubClasses = receiver.getSubClasses();
            for(Pair<String, String> subClass : dispatcherSubClasses) {
                if(receiverSubClasses.contains(subClass)) {
                    continue;
                }
                ((PushDownMethodObject) receiverObject).addSubClass(subClass);
            }
            for(Pair<String, String> subClass: receiverSubClasses) {
                if(dispatcherSubClasses.contains(subClass)) {
                    continue;
                }
                ((PushDownMethodObject) dispatcherObject).addSubClass(subClass);

            }
            return true;
        }
        return false;



    }

}
