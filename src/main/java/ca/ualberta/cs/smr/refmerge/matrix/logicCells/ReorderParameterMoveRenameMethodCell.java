package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.ReorderParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

import java.util.List;

public class ReorderParameterMoveRenameMethodCell {

    public static void checkCombination(RefactoringObject methodObject, RefactoringObject parameterObject) {
        ReorderParameterObject parameter = (ReorderParameterObject) parameterObject;
        MoveRenameMethodObject method = (MoveRenameMethodObject) methodObject;

        String originalParameterClass = parameter.getOriginalClass();
        String originalMethodClass = method.getOriginalClassName();
        String destinationParameterClass = parameter.getDestinationClass();
        String destinationMethodClass = method.getDestinationClassName();

        MethodSignatureObject originalParameterMethodSignature = parameter.getOriginalMethod();
        MethodSignatureObject originalMethodSignature = method.getOriginalMethodSignature();
        MethodSignatureObject destinationParameterMethodSignature = parameter.getDestinationMethod();
        MethodSignatureObject destinationMethodSignature = method.getDestinationMethodSignature();

        List<ParameterObject> originalParameters = parameter.getOriginalParameterList();
        List<ParameterObject> reorderedParameters = parameter.getReorderedParameterList();

        // If the method was renamed and/or moved before the reorder parameters refactoring
        if(destinationMethodClass.equals(originalParameterClass)
                && destinationMethodSignature.equalsSignature(originalParameterMethodSignature)) {
            destinationMethodSignature.replaceParameterList(reorderedParameters);
            ((MoveRenameMethodObject) methodObject).setDestinationMethodSignature(destinationMethodSignature);

        }

        // If the reorder parameters refactoring happened before the move + rename method refactoring
        else if(originalMethodClass.equals(destinationParameterClass)
                && originalMethodSignature.equalsSignature(destinationParameterMethodSignature)) {
            originalMethodSignature.replaceParameterList(originalParameters);
            ((MoveRenameMethodObject) methodObject).setOriginalMethodSignature(originalMethodSignature);
        }

    }

}
