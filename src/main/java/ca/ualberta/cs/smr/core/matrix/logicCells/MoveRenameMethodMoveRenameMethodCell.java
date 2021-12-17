package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;

/*
 * Contains the logic checks for rename method/rename method conflicts.
 */
public class MoveRenameMethodMoveRenameMethodCell {
    final private Project project;

    public MoveRenameMethodMoveRenameMethodCell(Project project) {
        this.project = project;
    }

    /*
     *  Check if a rename method refactoring conflicts with a second rename method refactoring. Rename method/rename method
     *  can result in an override conflict, an overload conflict, or a naming conflict.
     */
    public boolean moveRenameMethodMoveRenameMethodConflictCell(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        // Check for a method override conflict
        if(moveRenameMethodMoveRenameMethodCell.checkOverrideConflict(dispatcherObject, receiverObject)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(moveRenameMethodMoveRenameMethodCell.checkOverloadConflict(dispatcherObject, receiverObject)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(dispatcherObject, receiverObject)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    /*
     * If two methods are renamed to the same name with a different signature in classes that have an inheritance relationship,
     * then they were likely part of an accidental override
     */
    public boolean checkOverrideConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameMethodObject dispatcherRenameMethod = ((MoveRenameMethodObject) dispatcherObject);
        MoveRenameMethodObject receiverRenameMethod = ((MoveRenameMethodObject) receiverObject);
        // Get the original operations
        MethodSignatureObject dispatcherOriginalMethod = dispatcherRenameMethod.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverRenameMethod.getOriginalMethodSignature();
        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcherRenameMethod.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverRenameMethod.getDestinationMethodSignature();
        // Get the class names
        String dispatcherClassName = dispatcherRenameMethod.getOriginalClassName();
        String receiverClassName = receiverRenameMethod.getOriginalClassName();

        // If the rename methods happen in the same class then there is no override conflict
        if(dispatcherClassName.equals(receiverClassName)) {
            return false;
        }
        String dispatcherFile = dispatcherRenameMethod.getOriginalFilePath();
        String receiverFile = receiverRenameMethod.getOriginalFilePath();
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
        // Check if the methods start with the same name and end with different names, or if they end with the same name
        // and start with different names. If they do, then there's a likely override conflict.
        return !isSameName(dispatcherOriginalMethodName, receiverOriginalMethodName) &&
                isSameName(dispatcherNewMethodName, receiverNewMethodName) &&
                dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }

    /*
     * Check if both branches refactored two methods with different signatures to the same name. If they did, this is a
     * possible accidental overloading conflict.
     */
    public boolean checkOverloadConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        MoveRenameMethodObject dispatcherRenameMethod = ((MoveRenameMethodObject) dispatcherObject);
        MoveRenameMethodObject receiverRenameMethod = ((MoveRenameMethodObject) receiverObject);
        // Get the original operations
        MethodSignatureObject dispatcherOriginalMethod = dispatcherRenameMethod.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverRenameMethod.getOriginalMethodSignature();
        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcherRenameMethod.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverRenameMethod.getDestinationMethodSignature();
        // Get class names
        String dispatcherClassName = dispatcherRenameMethod.getOriginalClassName();
        String receiverClassName = receiverRenameMethod.getOriginalClassName();
        // If the methods are in different classes, no overloading happens
        if (!dispatcherClassName.equals(receiverClassName)) {
            Utils utils = new Utils(project);
            String dispatcherFile = dispatcherRenameMethod.getOriginalFilePath();
            String receiverFile = receiverRenameMethod.getOriginalFilePath();
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
        String receiverDestinationMethodname = receiverDestinationMethod.getName();
        // If two methods with different signatures are renamed to the same method, this overloading conflict
        return (!dispatcherOriginalMethodName.equals(receiverOriginalMethodName) &&
                dispatcherDestinationMethodName.equals(receiverDestinationMethodname)) &&
                !dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }

    /*
     * Check for two methods being refactored to the same signature or one method being refactored to two methods.
     */
    public boolean checkMethodNamingConflict(RefactoringObject dispatcherRefactoring, RefactoringObject receiverRefactoring) {
        MoveRenameMethodObject dispatcherObject = (MoveRenameMethodObject) dispatcherRefactoring;
        MoveRenameMethodObject receiverObject = (MoveRenameMethodObject) receiverRefactoring;

        MethodSignatureObject dispatcherOriginalMethod = dispatcherObject.getOriginalMethodSignature();
        MethodSignatureObject dispatcherDestinationMethod = dispatcherObject.getDestinationMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverObject.getOriginalMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverObject.getDestinationMethodSignature();

        String dispatcherOriginalClass = dispatcherObject.getOriginalClassName();
        String dispatcherDestinationClass = dispatcherObject.getDestinationClassName();
        String receiverOriginalClass = receiverObject.getOriginalClassName();
        String receiverDestinationClass = receiverObject.getDestinationClassName();

        // If both objects are rename method operations or move method operations
        if((dispatcherObject.isRenameMethod() && receiverObject.isRenameMethod())
                || (dispatcherObject.isMoveMethod() && receiverObject.isMoveMethod())) {
            // Check if the original method signature and class are the same
            if(dispatcherOriginalClass.equals(receiverOriginalClass)
                    && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
                // Check if the new class or method signature are different
                // A rename method or move method operation could be part of a move+rename method operation so check the class
                // and method signature
                return !dispatcherDestinationClass.equals(receiverDestinationClass)
                        || !dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
            }
            // If the original method signature and class are different
            else {
                // Check if the refactored method is in the same class with the same signature
                // Two methods that were moved/renamed to the same method in the same class is conflicting
                return dispatcherDestinationClass.equals(receiverDestinationClass)
                        && dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
            }
        }

        return false;



    }

    /*
     * Check for dependence between rename method and move method operations specifically.
     */
    public boolean checkDependence(RefactoringObject dispatcherRefactoring, RefactoringObject receiverRefactoring) {
        MoveRenameMethodObject dispatcherObject = (MoveRenameMethodObject) dispatcherRefactoring;
        MoveRenameMethodObject receiverObject = (MoveRenameMethodObject) receiverRefactoring;

        MethodSignatureObject dispatcherOriginalMethod = dispatcherObject.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverObject.getOriginalMethodSignature();

        // If both refactoring objects do not have the same original method signature, then there cannot be dependence
        // between them.
        if(!dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return false;
        }
        // If both refactoring types are move method refactorings, then there cannot be dependence between them
        if(dispatcherObject.isMoveMethod() && receiverObject.isMoveMethod()) {
            return false;
        }
        // If both refactorings are not rename method, one must be move method and the other must be rename method.
        // Since they both have the same signature and they are rename method/move method only, they have dependence
        return !(dispatcherObject.isRenameMethod() && receiverObject.isRenameMethod());

    }

    /*
     * Check if the second refactoring is a transitive refactoring of the first refactoring.
     */
    public boolean checkTransitivity(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        boolean isTransitive = false;
        MoveRenameMethodObject firstObject = (MoveRenameMethodObject) firstRefactoring;
        MoveRenameMethodObject secondObject = (MoveRenameMethodObject) secondRefactoring;
        String firstDestinationClass = firstObject.getDestinationClassName();
        MethodSignatureObject firstDestinationMethod = firstObject.getDestinationMethodSignature();
        String secondOriginalClass = secondObject.getOriginalClassName();
        MethodSignatureObject secondOriginalMethod = secondObject.getOriginalMethodSignature();
        String secondDestinationClass = secondObject.getDestinationClassName();
        // If the renamed method of the first refactoring and original method of the second refactoring are the same
        if(firstDestinationClass.equals(secondOriginalClass) && firstDestinationMethod.equalsSignature(secondOriginalMethod)) {
            //This is a transitive refactoring
            isTransitive = true;
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((MoveRenameMethodObject) firstRefactoring).setDestinationClassName(secondObject.getDestinationClassName());
            ((MoveRenameMethodObject) firstRefactoring).setDestinationMethodSignature(secondObject.getDestinationMethodSignature());
            ((MoveRenameMethodObject) firstRefactoring).setOriginalDestinationClassName(secondObject.getOriginalDestinationClassName());
        }
        else if(firstDestinationClass.equals(secondDestinationClass) && firstDestinationMethod.equalsSignature(secondOriginalMethod)) {
            isTransitive = true;
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((MoveRenameMethodObject) firstRefactoring).setDestinationClassName(secondObject.getDestinationClassName());
            ((MoveRenameMethodObject) firstRefactoring).setDestinationMethodSignature(secondObject.getDestinationMethodSignature());
            ((MoveRenameMethodObject) firstRefactoring).setOriginalDestinationClassName(secondObject.getOriginalDestinationClassName());
        }

        return isTransitive;
    }



}
