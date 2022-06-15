package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.ReorderParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

import java.util.List;

public class ReorderParameterExtractMethodCell {

    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        ReorderParameterObject parameter = (ReorderParameterObject) parameterObject;
        ExtractMethodObject method = (ExtractMethodObject) methodObject;

        String originalParameterClass = parameter.getOriginalClass();
        String originalMethodClass = method.getOriginalClassName();
        String destinationParameterClass = parameter.getDestinationClass();
        String destinationMethodClass = method.getDestinationClassName();

        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethod();
        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();

        List<ParameterObject> reorderedParameters = parameter.getReorderedParameterList();

        // If the source method's parameters are reordered
        if(destinationMethodClass.equals(originalParameterClass)
                && originalMethodSignature.equalsSignature(originalParameterMethodSignature)) {
            originalMethodSignature.replaceParameterList(reorderedParameters);
            ((ExtractMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);

        }

        // If the extracted method's parameters are reordered
        else if(originalMethodClass.equals(destinationParameterClass)
                && destinationMethodSignature.equalsSignature(originalParameterMethodSignature)) {
            destinationMethodSignature.replaceParameterList(reorderedParameters);
            ((ExtractMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);
        }

    }

}
