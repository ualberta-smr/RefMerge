package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

public class RenameParameterExtractMethodCell {

    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        ExtractMethodObject method = (ExtractMethodObject) methodObject;
        RenameParameterObject parameter = (RenameParameterObject) parameterObject;

        String originalMethodClass = method.getOriginalClassName();
        String destinationMethodClass = method.getDestinationClassName();

        String originalParameterClass = parameter.getOriginalClassName();
        String destinationParameterClass = parameter.getRefactoredClassName();

        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();
        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethodSignature();
        MethodSignatureObject destinationParameterMethodSignature = parameter.getRefactoredMethodSignature();

        ParameterObject destinationParameter = parameter.getRefactoredParameterObject();

        // If the extract method happens before the parameter refactoring
        // If the parameter refactoring is performed on the source method
        // em extracted from sm & sm.p1 -> sm.p2
        // Then update the original parameter's location and update the source method signature
        if(originalMethodClass.equals(destinationParameterClass)
                && originalMethodSignature.equalsSignatureExcludingParameterNames(originalParameterMethodSignature)) {
            // Update parameter object
            originalParameterMethodSignature.setName(originalMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setOriginalMethodSignature(originalParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setOriginalClassName(originalMethodClass);
            // Update source method
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            originalMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((ExtractMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }
        // If the parameter refactoring is performed on the extracted method
        // em.p1 -> em.p2 & em extracted from sm
        // Then update the original parameter's location and update the extract method signature
        else if(destinationMethodClass.equals(originalParameterClass)
                && destinationMethodSignature.equalsSignatureExcludingParameterNames(originalParameterMethodSignature)) {
            // Update parameter object
            originalParameterMethodSignature.setName(destinationMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setOriginalMethodSignature(originalMethodSignature);
            // Update source method
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            destinationMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((ExtractMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);
        }
        // If the extract method happens after the parameter refactoring
        // If the parameter refactoring is performed on the source method
        // sm.p1 -> sm.p2 & em extracted from sm
        // Then update the destination parameter's location and update the original source method signature
        else if(originalMethodClass.equals(originalParameterClass)
                && originalMethodSignature.equalsSignatureExcludingParameterNames(originalParameterMethodSignature)) {
            // Update parameter object
            destinationParameterMethodSignature.setName(originalMethodSignature.getName());
            ((RenameParameterObject) parameterObject).setRefactoredMethodSignature(destinationParameterMethodSignature);
            ((RenameParameterObject) parameterObject).setRefactoredClassName(destinationMethodClass);
            // Update source method
            int location = destinationParameterMethodSignature.getParameterLocation(destinationParameter);
            originalMethodSignature.updateParameterAtLocation(location, destinationParameter);
            ((ExtractMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }
    }
}
