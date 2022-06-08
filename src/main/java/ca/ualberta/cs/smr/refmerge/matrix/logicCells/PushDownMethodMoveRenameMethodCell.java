package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class PushDownMethodMoveRenameMethodCell {

    Project project;

    public PushDownMethodMoveRenameMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) dispatcher;
        PushDownMethodObject pushDownMethodObject = (PushDownMethodObject) receiver;
        // Override conflict
        if(overrideConflict(moveRenameMethodObject, pushDownMethodObject)) {
            return true;
        }
        // Overload conflict
        if(overloadConflict(moveRenameMethodObject, pushDownMethodObject)) {
            return true;
        }
        // Naming conflict
        return namingConflict(moveRenameMethodObject, pushDownMethodObject);
    }

    public boolean overrideConflict(MoveRenameMethodObject dispatcher, PushDownMethodObject receiver) {

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();
        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();
        // Get the class names
        String dispatcherClassName = dispatcher.getDestinationClassName();
        String receiverClassName = receiver.getTargetBaseClass();

        // If the rename methods happen in the same class then there is no override conflict
        if(dispatcherClassName.equals(receiverClassName)) {
            return false;
        }
        String dispatcherFile = dispatcher.getDestinationFilePath();
        String receiverFile = receiver.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
        if(psiReceiver != null && psiDispatcher != null) {
            if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }
        // Get original method names
        String dispatcherOriginalMethodName = dispatcherOriginalMethod.getName();
        String receiverOriginalMethodName = receiverOriginalMethod.getName();
        // get new method names
        String dispatcherNewMethodName = dispatcherDestinationMethod.getName();
        String receiverNewMethodName = receiverDestinationMethod.getName();
        // Check if the methods end with the same name and start with different names.
        // If they do, then there's a likely override conflict.
        return !isSameName(dispatcherOriginalMethodName, receiverOriginalMethodName) &&
                isSameName(dispatcherNewMethodName, receiverNewMethodName) &&
                dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);

    }

    public boolean overloadConflict(MoveRenameMethodObject dispatcher, PushDownMethodObject receiver) {

        // Get the original operations
        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();
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
        String dispatcherOriginalMethodName = dispatcherOriginalMethod.getName();
        String dispatcherDestinationMethodName = dispatcherDestinationMethod.getName();
        String receiverOriginalMethodName = receiverOriginalMethod.getName();
        String receiverDestinationMethodName = receiverDestinationMethod.getName();
        // If two methods with different signatures are refactored to the same method name, this overloading conflict
        return (!dispatcherOriginalMethodName.equals(receiverOriginalMethodName) &&
                dispatcherDestinationMethodName.equals(receiverDestinationMethodName)) &&
                !dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);

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

    public void checkCombination(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameMethodObject dispatcher = (MoveRenameMethodObject) dispatcherObject;
        PushDownMethodObject receiver = (PushDownMethodObject) receiverObject;

        String dispatcherOriginalClass = dispatcher.getOriginalClassName();
        String dispatcherDestinationClass = dispatcher.getDestinationClassName();
        String receiverOriginalClass = receiver.getOriginalClass();
        String receiverDestinationClass = receiver.getTargetBaseClass();

        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject dispatcherDestinationMethod = dispatcher.getDestinationMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiver.getDestinationMethodSignature();

        // If the move+rename method refactoring happens before the push down method refactoring,
        // The refactored name+class will equal the push down method's original name+class
        if(dispatcherDestinationClass.equals(receiverOriginalClass)
                && dispatcherDestinationMethod.equalsSignature(receiverOriginalMethod)) {
            // Update the original method signature for push down method to check for conflicts
            ((PushDownMethodObject) receiverObject).setOriginalMethodSignature(dispatcherOriginalMethod);
            // Update the original class for push down method to check for conflicts
            receiverObject.setOriginalFilePath(dispatcherObject.getOriginalFilePath());
            ((PushDownMethodObject) receiverObject).setOriginalClass(dispatcherOriginalClass);
            // Update the refactored method signature and class name for move+rename method so we can find future
            // refactorings that might change the same program element
            ((MoveRenameMethodObject) dispatcherObject).setDestinationMethodSignature(receiverDestinationMethod);
            dispatcherObject.setDestinationFilePath(receiverObject.getDestinationFilePath());
            ((MoveRenameMethodObject) dispatcherObject).setDestinationClassName(receiverDestinationClass);



        }


        // If the push down method happens before the move+rename method, the refactored push down method's name+class
        // will equal the move+rename method's original name+class
        if(dispatcherOriginalClass.equals(receiverDestinationClass)
                && dispatcherOriginalMethod.equalsSignature(receiverDestinationMethod)) {
            // Update the destination method and class for the push down method refactoring
            ((PushDownMethodObject) receiverObject).setDestinationMethodSignature(dispatcherDestinationMethod);
            receiverObject.setDestinationFilePath(dispatcherObject.getDestinationFilePath());
            ((PushDownMethodObject) receiverObject).setTargetBaseClass(dispatcherDestinationClass);
            // Update the original method and class for the move+rename method refactoring
            ((MoveRenameMethodObject) dispatcherObject).setOriginalMethodSignature(receiverOriginalMethod);
            dispatcherObject.setOriginalFilePath(receiverObject.getOriginalFilePath());
            ((MoveRenameMethodObject) dispatcherObject).setOriginalClassName(receiverOriginalClass);

        }


    }

}
