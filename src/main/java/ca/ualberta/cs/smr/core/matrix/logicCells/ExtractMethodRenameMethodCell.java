package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;

/*
 * Contains the logic check for extract method/rename method refactoring conflict and ordering dependence checks.
 */
public class ExtractMethodRenameMethodCell {
    Project project;

    public ExtractMethodRenameMethodCell(Project project) {
        this.project = project;
    }

    /*
     *  Check if a refactoring conflict exists between extract method/rename method refactorings. A refactoring conflict
     *  can occur if there is an accidental override, accidental overload, or naming conflict.
     *  @param dispatcherNode: A node containing the dispatcher rename method refactoring.
     *  @param receiverNode: A node containing the receiver extract method refactoring.
     */
    public boolean extractMethodRenameMethodConflictCell(Node dispatcherNode, Node receiverNode) {
        // Extract Method/Rename Method override conflict
        if(checkOverrideConflict(dispatcherNode, receiverNode)) {
            return true;
        }
        // Extract Method/Rename Method overload conflict
        else if(checkOverloadConflict(dispatcherNode, receiverNode)) {
            return true;
        }

        // Extract Method/Rename Method naming conflict
        else return checkMethodNamingConflict(dispatcherNode, receiverNode);
    }

    /*
     *  Check if an ordering dependence exists between extract method/rename method refactorings.
     *  @param dispatcherNode: A node containing the dispatcher rename method refactoring.
     *  @param receiverNode: A node containing the receiver extract method refactoring.
     */
    public boolean extractMethodRenameMethodDependenceCell(Node dispatcherNode, Node receiverNode) {
        return checkExtractMethodRenameMethodDependence(dispatcherNode, receiverNode);
    }

    public boolean checkOverrideConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring renameMethodRefactoring = dispatcherNode.getRefactoring();
        Refactoring extractMethodRefactoring = receiverNode.getRefactoring();

        // Get the refactored operations
        UMLOperation renamedOperation = getRefactoredRenameOperation(renameMethodRefactoring);
        UMLOperation extractedOperation = ((ExtractOperationRefactoring) extractMethodRefactoring).getExtractedOperation();
        // Get the class names
        String renamedMethodClassName = dispatcherNode.getDependenceChainClassHead();
        String extractedMethodClassName = receiverNode.getDependenceChainClassHead();

        // If the rename methods happen in the same class then there is no override conflict
        if(isSameName(renamedMethodClassName, extractedMethodClassName)) {
            if(isSameOriginalClass(dispatcherNode, receiverNode)) {
                return false;
            }
        }
        Utils utils = new Utils(project);
        String renameMethodFile = renamedOperation.getLocationInfo().getFilePath();
        String extractMethodFile = extractedOperation.getLocationInfo().getFilePath();
        PsiClass renameMethodPsiClass = utils.getPsiClassByFilePath(renameMethodFile, renamedMethodClassName);
        PsiClass extractMethodPsiClass = utils.getPsiClassByFilePath(extractMethodFile, extractedMethodClassName);
        if(!ifClassExtends(renameMethodPsiClass, extractMethodPsiClass)) {
            return false;
        }

        if(!renamedOperation.equalSignature(extractedOperation)) {
            return false;
        }

        String renamedMethodName = renamedOperation.getName();
        String extractedMethodName = extractedOperation.getName();

        // If the signatures are different, then it cannot be an accidental override
        if(!renamedOperation.equalSignature(extractedOperation)) {
            return false;
        }
        // If the signatures are the same and the names are the same then it is a case of accidental overriding
        return renamedMethodName.equals(extractedMethodName);
    }

    public boolean checkOverloadConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring renameMethodRefactoring = dispatcherNode.getRefactoring();
        Refactoring extractMethodRefactoring = receiverNode.getRefactoring();

        UMLOperation renameMethodOperation = getRefactoredRenameOperation(renameMethodRefactoring);
        UMLOperation extractMethodOperation = ((ExtractOperationRefactoring) extractMethodRefactoring).getExtractedOperation();
        // Get class names
        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
        String receiverClassName = receiverNode.getDependenceChainClassHead();
        // If the methods are in different classes, check if one class inherits the other
        if (!isSameName(dispatcherClassName, receiverClassName)) {
            Utils utils = new Utils(project);
            String renameMethodFile = renameMethodOperation.getLocationInfo().getFilePath();
            String extractMethodFile = extractMethodOperation.getLocationInfo().getFilePath();
            PsiClass renameMethodPsiClass = utils.getPsiClassByFilePath(renameMethodFile, dispatcherClassName);
            PsiClass extractMethodPsiClass = utils.getPsiClassByFilePath(extractMethodFile, receiverClassName);
            if(!ifClassExtends(renameMethodPsiClass, extractMethodPsiClass)) {
                return false;
            }
        }
        String renamedMethodName = renameMethodOperation.getName();
        String extractedMethodName = extractMethodOperation.getName();

        // If the signatures are equal, then it is not an accidental overload
        if(renameMethodOperation.equalSignature(extractMethodOperation)) {
            return false;
        }
        // If the signatures are different and the names are the same, it is an accidental overload
        return renamedMethodName.equals(extractedMethodName);
    }

    public boolean checkMethodNamingConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring renameMethodRefactoring = dispatcherNode.getRefactoring();
        Refactoring extractMethodRefactoring = receiverNode.getRefactoring();
        UMLOperation renamedMethodOperation = ((RenameOperationRefactoring) renameMethodRefactoring).getRenamedOperation();
        UMLOperation extractedMethodOperation = ((ExtractOperationRefactoring) extractMethodRefactoring).getExtractedOperation();
//        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
//        String receiverClassName = receiverNode.getDependenceChainClassHead();
        String renamedMethodClassName = renamedMethodOperation.getClassName();
        String extractedMethodClassName = extractedMethodOperation.getClassName();

        // If the methods are in different classes
        if (!isSameName(renamedMethodClassName, extractedMethodClassName)) {
            if(!isSameOriginalClass(dispatcherNode, receiverNode))
                return false;
        }

        if(!renamedMethodOperation.equalSignature(extractedMethodOperation)) {
            return false;
        }
        String renamedMethodName = getRefactoredMethodName(renameMethodRefactoring);
        String extractedMethodName = ((ExtractOperationRefactoring) extractMethodRefactoring).getExtractedOperation().getName();
        // Check if the extracted method name is the same as the renamed method name
        return renamedMethodName.equals(extractedMethodName);
    }

    public static boolean checkExtractMethodRenameMethodDependence(Node dispatcherNode, Node receiverNode) {
        Refactoring renameMethodRefactoring = dispatcherNode.getRefactoring();
        Refactoring extractMethodRefactoring = receiverNode.getRefactoring();
        UMLOperation originalMethodOperation = ((RenameOperationRefactoring) renameMethodRefactoring).getOriginalOperation();
        UMLOperation sourceMethodOperation = ((ExtractOperationRefactoring) extractMethodRefactoring).getSourceOperationBeforeExtraction();

        String originalMethodClassName = originalMethodOperation.getClassName();
        String sourceMethodClassName = sourceMethodOperation.getClassName();

        // If the methods are in different classes then there cannot be ordering dependence
        if (!isSameName(originalMethodClassName, sourceMethodClassName)) {
            if(!isSameOriginalClass(dispatcherNode, receiverNode))
                return false;
        }

        // If the signatures are the same and they are in the same class, then the source method and original method
        // must be the same
        return originalMethodOperation.equalSignature(sourceMethodOperation);
    }

    /*
     * Check for extract method and rename method transitivity and combinations. If there is transitivity, update the
     * extracted method and return true. If there is a combination, update the extracted method and return false.
     */
    public static boolean checkExtractMethodRenameMethodTransitivity(RefactoringObject renameMethod,
                                                                     RefactoringObject extractMethod) {
        boolean isTransitive = false;
        RenameMethodObject renameMethodObject = (RenameMethodObject) renameMethod;
        String originalRenameClassName = renameMethodObject.getOriginalClassName();
        String destinationRenameClassName = renameMethodObject.getDestinationClassName();
        String originalRenameName = renameMethodObject.getOriginalMethodName();
        String destinationRenameName = renameMethodObject.getDestinationMethodName();

        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String originalExtractClassName = extractMethodObject.getOriginalClassName();
        String destinationExtractClassName = extractMethodObject.getDestinationClassName();
        String originalExtractName = extractMethodObject.getOriginalMethodName();
        String destinationExtractName = extractMethodObject.getDestinationMethodName();


        // If the source method and destination method are the same in extract method and rename method, then the
        // extracted method was actually extracted from the original method in the rename method refactoring (for conflict
        // checks). Update the source method details to use the original method.
        if(destinationRenameName.equals(originalExtractName) && destinationRenameClassName.equals(originalExtractClassName)) {
            extractMethod.setOriginalFilePath(renameMethodObject.getOriginalFilePath());
            ((ExtractMethodObject) extractMethod).setOriginalClassName(renameMethodObject.getOriginalClassName());
            ((ExtractMethodObject) extractMethod).setOriginalMethodName(renameMethodObject.getOriginalMethodName());
        }
        // If the original name of the rename method and the extracted method name are the same, then the method was extracted
        // and then renamed. This is a transitive refactoring so we update the extract method refactoring with the new name
        // of the extracted method.
        else if(originalRenameName.equals(destinationExtractName) && originalRenameClassName.equals(destinationExtractClassName)) {
            isTransitive = true;
            extractMethod.setDestinationFilePath(renameMethodObject.getDestinationFilePath());
            ((ExtractMethodObject) extractMethod).setDestinationClassName(renameMethodObject.getDestinationClassName());
            ((ExtractMethodObject) extractMethod).setDestinationMethodName(renameMethodObject.getDestinationMethodName());
        }


        return isTransitive;
    }
}
