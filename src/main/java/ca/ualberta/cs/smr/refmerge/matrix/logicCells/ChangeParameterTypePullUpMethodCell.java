package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ChangeParameterTypeObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

public class ChangeParameterTypePullUpMethodCell {
    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        ChangeParameterTypeObject parameter = (ChangeParameterTypeObject) parameterObject;
        PullUpMethodObject method = (PullUpMethodObject) methodObject;

        String originalParameterClass = parameter.getOriginalClass();
        String originalMethodClass = method.getOriginalClass();
        String destinationParameterClass = parameter.getDestinationClass();
        String destinationMethodClass = method.getTargetClass();

        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethod();
        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationParameterMethodSignature = parameter.getDestinationMethod();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();

        ParameterObject originalParameter = parameter.getOriginalParameter();
        ParameterObject destinationParameter = parameter.getDestinationParameter();

        // If the method was pulled up before the change parameter type refactoring
        if(destinationMethodClass.equals(originalParameterClass)
                && destinationMethodSignature.equalsSignature(originalParameterMethodSignature)) {
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            destinationMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((PullUpMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);

        }

        // If the change parameter type refactoring happened before the pull up method refactoring
        else if(originalMethodClass.equals(destinationParameterClass)
                && originalMethodSignature.equalsSignature(destinationParameterMethodSignature)) {
            int location = originalParameterMethodSignature.getParameterLocation(originalParameter);
            originalMethodSignature.updateParameterAtLocation(location, originalParameter);
            ((PullUpMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }

    }
}
