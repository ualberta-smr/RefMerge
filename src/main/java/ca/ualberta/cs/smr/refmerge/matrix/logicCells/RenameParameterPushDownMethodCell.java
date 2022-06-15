package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

public class RenameParameterPushDownMethodCell {

    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        PushDownMethodObject method = (PushDownMethodObject) methodObject;
        RenameParameterObject parameter = (RenameParameterObject) parameterObject;

        String originalMethodClass = method.getOriginalClass();
        String destinationMethodClass = method.getTargetBaseClass();
        String originalParameterClass = parameter.getOriginalClassName();
        String destinationParameterClass = parameter.getRefactoredClassName();

        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();
        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethodSignature();
        MethodSignatureObject destinationParameterMethodSignature = parameter.getRefactoredMethodSignature();

        ParameterObject originalParameter = parameter.getOriginalParameterObject();
        ParameterObject destinationParameter = parameter.getRefactoredParameterObject();

        // If the rename parameter refactoring happens after the push down method refactoring
        if(destinationMethodClass.equals(originalParameterClass)
                && destinationMethodSignature.equalsSignatureExcludingParameterNames(originalParameterMethodSignature)) {
            // Update original parameter
            originalParameterMethodSignature.setName(originalMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setOriginalMethodSignature(originalParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setOriginalClassName(originalMethodClass);
            // Update refactored method
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            destinationMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((PushDownMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);
        }

        // If the rename parameter refactoring happens before the push down method refactoring
        if(originalMethodClass.equals(destinationParameterClass)
                && originalMethodSignature.equalsSignature(destinationParameterMethodSignature)) {
            // Update refactored parameter
            destinationParameterMethodSignature.setName(destinationMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setRefactoredMethodSignature(destinationParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setRefactoredClassName(destinationMethodClass);
            // Update original method
            int location = originalParameterMethodSignature.getParameterLocation(originalParameter);
            originalMethodSignature.updateParameterAtLocation(location, originalParameter);
            ((PushDownMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }
    }

}
