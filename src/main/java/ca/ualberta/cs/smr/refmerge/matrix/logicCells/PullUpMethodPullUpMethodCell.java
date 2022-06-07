package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

import java.util.List;

public class PullUpMethodPullUpMethodCell {

    Project project;

    public PullUpMethodPullUpMethodCell(Project project) {
        this.project = project;
    }

    /*
     * Check if Pull up method/Pull up method can result in a naming conflict
     */
    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {

        return checkMethodNamingConflict(dispatcher, receiver);
    }

    private boolean checkMethodNamingConflict(RefactoringObject dispatcher, RefactoringObject receiver) {
        PullUpMethodObject dispatcherObject = (PullUpMethodObject) dispatcher;
        PullUpMethodObject receiverObject = (PullUpMethodObject) receiver;

        String dispatcherOriginalClass = dispatcherObject.getOriginalClass();
        String receiverOriginalClass = receiverObject.getOriginalClass();

        String dispatcherTargetClass = dispatcherObject.getTargetClass();
        String receiverTargetClass = receiverObject.getTargetClass();

        // If the methods are not pulled up from the same class, it cannot result in a naming conflict
        // because methods pulled up from different classes that are the same can be combined
        if(!dispatcherOriginalClass.equals(receiverOriginalClass)) {
            return false;
        }

        MethodSignatureObject dispatcherOriginalMethod = dispatcherObject.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverObject.getOriginalMethodSignature();
        MethodSignatureObject dispatcherRefactoredMethod = dispatcherObject.getDestinationMethodSignature();
        MethodSignatureObject receiverRefactoredMethod = receiverObject.getDestinationMethodSignature();

        // if the original method signature is not the same, it cannot be a naming conflict in this case
        if(!dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return false;
        }

        // If the same method is pulled up from the same source class to the same target class
        // it is the same refactoring and is not conflicting
        if(dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)
                && dispatcherRefactoredMethod.equalsSignature(receiverRefactoredMethod)
                    && dispatcherTargetClass.equals(receiverTargetClass)) {
            return false;
        }

        // If the same method is pulled up to two different classes, report a naming conflict
        return dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)
                && !dispatcherTargetClass.equals(receiverTargetClass);
    }

    public boolean checkTransitivity(RefactoringObject receiverObject, RefactoringObject dispatcherObject) {
        PullUpMethodObject dispatcher = (PullUpMethodObject) dispatcherObject;
        PullUpMethodObject receiver = (PullUpMethodObject) receiverObject;


        String dispatcherTargetClass = dispatcher.getTargetClass();
        String receiverTargetClass = receiver.getTargetClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();


        // If the two pull up method refactorings are targeting different super classes, there is no transitivity
        if(!dispatcherTargetClass.equals(receiverTargetClass)) {
            return false;
        }

        // If the target class is the same and the method signatures are the same, then it is transitive
        // and we need to combine their subclass lists
        if(dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            List<Pair<String, String>> dispatcherSubClasses = dispatcher.getSubClasses();
            List<Pair<String, String>> receiverSubClasses = receiver.getSubClasses();
            for(Pair<String, String> subClass : dispatcherSubClasses) {
                if(receiverSubClasses.contains(subClass)) {
                    continue;
                }
                ((PullUpMethodObject) receiverObject).addSubClass(subClass);
            }
            for(Pair<String, String> subClass: receiverSubClasses) {
                boolean found = false;
                if(dispatcherSubClasses.contains(subClass)) {
                    continue;
                }
                ((PullUpMethodObject) dispatcherObject).addSubClass(subClass);

            }
            return true;
        }
        return false;

    }

}
