package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import java.util.Set;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;

/*
 * Contains the logic check for extract method/extract method refactoring conflict checks.
 */
public class ExtractMethodExtractMethodCell {
    Project project;
    public ExtractMethodExtractMethodCell(Project project) {
        this.project = project;
    }

    /*
     *  Check if a refactoring conflict exists between extract method/rename method refactorings. A refactoring conflict
     *  can occur if there is an overlapping fragments, accidental override, accidental overload, or naming conflict.
     *  @param dispatcherNode: A node containing the dispatcher extract method refactoring.
     *  @param receiverNode: A node containing the receiver extract method refactoring.
     */
    public boolean extractMethodExtractMethodConflictCell(Node dispatcherNode, Node receiverNode) {
        // Extract Method/Extract Method overlapping fragments conflict
        if(checkOverlappingFragmentsConflict(dispatcherNode, receiverNode)) {
            return true;
        }
        // Extract Method/Extract Method accidental override conflict
        else if(checkOverrideConflict(dispatcherNode, receiverNode)) {
            return true;
        }
        // Extract Method/Extract Method accidental override conflict
        else if(checkOverloadConflict(dispatcherNode, receiverNode)) {
            return true;
        }
        // Extract Method/Extract Method naming conflict
        return checkMethodNamingConflict(dispatcherNode, receiverNode);
    }

    /*
     * An overlapping fragments conflict will occur if the source method of both extract method refactorings is the same
     * and the extracted fragments overlap. The extracted fragments overlap if a code fragment in the source method is
     * extracted to both extracted methods.
     */
    public boolean checkOverlappingFragmentsConflict(Node dispatcherNode, Node receiverNode) {
        ExtractOperationRefactoring dispatcherRefactoring = (ExtractOperationRefactoring) dispatcherNode.getRefactoring();
        ExtractOperationRefactoring receiverRefactoring = (ExtractOperationRefactoring) receiverNode.getRefactoring();

        // Get the refactored operations
        UMLOperation sourceDispatcher = dispatcherRefactoring.getSourceOperationBeforeExtraction();
        UMLOperation sourceReceiver = receiverRefactoring.getSourceOperationBeforeExtraction();

        // If the source methods are in different classes or are not the same method, return false
        if(!sourceDispatcher.equalsQualified(sourceReceiver)) {
            return false;
        }

        Set<AbstractCodeFragment> dispatcherFragments = dispatcherRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        Set<AbstractCodeFragment> receiverFragments = receiverRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        // Check if a code fragment exists in both extracted methods
        for(AbstractCodeFragment dispatcherFragment : dispatcherFragments) {
            for(AbstractCodeFragment receiverFragment : receiverFragments) {
                // Use the text of the fragment instead of the line number because the line numbers may differ between
                // branches.
                if (dispatcherFragment.equalFragment(receiverFragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * An override conflict will occur between two extract method refactorings if the extracted methods have the same signature
     * and their classes are in an inheritance relationship.
     */
    public boolean checkOverrideConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRefactoring = dispatcherNode.getRefactoring();
        Refactoring receiverRefactoring = receiverNode.getRefactoring();

        // Get the refactored operations
        UMLOperation dispatcherOperation = ((ExtractOperationRefactoring) dispatcherRefactoring).getExtractedOperation();
        UMLOperation receiverOperation = ((ExtractOperationRefactoring) receiverRefactoring).getExtractedOperation();
        // Get the class names
        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
        String receiverClassName = receiverNode.getDependenceChainClassHead();

        // If the extract methods happen in the same class then there is no override conflict
        if(isSameName(dispatcherClassName, receiverClassName)) {
            if(isSameOriginalClass(dispatcherNode, receiverNode)) {
                return false;
            }
        }
        Utils utils = new Utils(project);
        String dispatcherFile = dispatcherOperation.getLocationInfo().getFilePath();
        String receiverFile = receiverOperation.getLocationInfo().getFilePath();
        PsiClass dispatcherPsiClass = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
        PsiClass receiverPsiClass = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
        if(!ifClassExtends(dispatcherPsiClass, receiverPsiClass)) {
            return false;
        }

        if(!dispatcherOperation.equalSignature(receiverOperation)) {
            return false;
        }

        String dispatcherMethodName = dispatcherOperation.getName();
        String receiverMethodName = receiverOperation.getName();

        // If the signatures are different, then it cannot be an accidental override
        if(!dispatcherOperation.equalSignature(receiverOperation)) {
            return false;
        }
        // If the signatures are the same and the names are the same then it is a case of accidental overriding
        return dispatcherMethodName.equals(receiverMethodName);
    }

    /*
     * An overload conflict will occur between two extract method refactorings if the extracted methods have the same name and
     * different signatures. They need to either be in the same class, or in two classes that have an inheritance relationship.
     */
    public boolean checkOverloadConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRefactoring = dispatcherNode.getRefactoring();
        Refactoring receiverRefactoring = receiverNode.getRefactoring();

        UMLOperation dispatcherOperation = ((ExtractOperationRefactoring) dispatcherRefactoring).getExtractedOperation();
        UMLOperation receiverOperation = ((ExtractOperationRefactoring) receiverRefactoring).getExtractedOperation();
        // Get class names
        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
        String receiverClassName = receiverNode.getDependenceChainClassHead();
        // If the methods are in different classes, check if one class inherits the other
        if (!isSameName(dispatcherClassName, receiverClassName)) {
            Utils utils = new Utils(project);
            String dispatcherFile = dispatcherOperation.getLocationInfo().getFilePath();
            String receiverFile = receiverOperation.getLocationInfo().getFilePath();
            PsiClass dispatcherPsiClass = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
            PsiClass receiverPsiClass = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
            if(!ifClassExtends(dispatcherPsiClass, receiverPsiClass)) {
                return false;
            }
        }
        String dispatcherMethodName = dispatcherOperation.getName();
        String receiverMethodName = receiverOperation.getName();

        // If the signatures are equal, then it is not an accidental overload
        if(dispatcherOperation.equalSignature(receiverOperation)) {
            return false;
        }
        // If the signatures are different and the names are the same, it is an accidental overload
        return dispatcherMethodName.equals(receiverMethodName);
    }

    /*
     * A naming conflict will occur between two extract method refactorings if the extracted methods exist in the same class
     * and they have the same signature.
     */
    public boolean checkMethodNamingConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRefactoring = dispatcherNode.getRefactoring();
        Refactoring receiverRefactoring = receiverNode.getRefactoring();
        UMLOperation dispatcherOperation = ((ExtractOperationRefactoring) dispatcherRefactoring).getExtractedOperation();
        UMLOperation receiverOperation = ((ExtractOperationRefactoring) receiverRefactoring).getExtractedOperation();
        String dispatcherClassName = dispatcherOperation.getClassName();
        String receiverClassName = receiverOperation.getClassName();

        // If the methods are in different classes
        if (!isSameName(dispatcherClassName, receiverClassName)) {
            if(!isSameOriginalClass(dispatcherNode, receiverNode))
                return false;
        }

        if(!dispatcherOperation.equalSignature(receiverOperation)) {
            return false;
        }
        String dispatcherMethodName = dispatcherOperation.getName();
        String receiverMethodName = receiverOperation.getName();
        // Check if the extracted method names are the same
        return dispatcherMethodName.equals(receiverMethodName);
    }


}
