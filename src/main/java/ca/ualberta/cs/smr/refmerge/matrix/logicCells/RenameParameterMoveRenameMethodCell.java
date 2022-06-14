package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

public class RenameParameterMoveRenameMethodCell {

    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        MoveRenameMethodObject method = (MoveRenameMethodObject) methodObject;
        RenameParameterObject parameter = (RenameParameterObject) parameterObject;

        String originalMethodClass = method.getOriginalClassName();
        String destinationMethodClass = method.getDestinationClassName();

        String originalParameterClass = parameter.getOriginalClassName();
        String destinationParameterClass = parameter.getRefactoredClassName();

        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();
        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethodSignature();
        MethodSignatureObject destinationParameterMethodSignature = parameter.getRefactoredMethodSignature();

        ParameterObject originalParameter = parameter.getOriginalParameterObject();
        ParameterObject destinationParameter = parameter.getRefactoredParameterObject();


        // If the method is refactored before the parameter refactoring
        // C1.m1.p1 -> C1.m2.p1 & C1.m2.p1 -> C1.m2.p2
        // Then update the original parameter's location and update the refactored method signature
        if(destinationMethodClass.equals(originalParameterClass)
                && destinationMethodSignature.equalsSignatureExcludingParameterNames(originalParameterMethodSignature)) {
            // Update parameter object
            originalParameterMethodSignature.setName(originalMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setOriginalMethodSignature(originalParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setOriginalClassName(originalMethodClass);
            // Update method object
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            destinationMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((MoveRenameMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);
        }

        // If the parameter refactoring takes place before the method refactoring
        // C1.m1.p1 -> C1.m1.p2 & C1.m1.p2 -> C1.m2.p2
        // Then update the refactored parameters location and update the original method signature
        else if(originalMethodClass.equals(destinationParameterClass)
                && originalMethodSignature.equalsSignatureExcludingParameterNames(destinationParameterMethodSignature)) {
            // Update parameter object
            destinationParameterMethodSignature.setName(destinationMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setRefactoredMethodSignature(destinationParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setRefactoredClassName(destinationMethodClass);
            // Update method object
            int location = originalParameterMethodSignature.getParameterLocation(originalParameter);
            originalMethodSignature.updateParameterAtLocation(location, originalParameter);
            ((MoveRenameMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }
    }
}
