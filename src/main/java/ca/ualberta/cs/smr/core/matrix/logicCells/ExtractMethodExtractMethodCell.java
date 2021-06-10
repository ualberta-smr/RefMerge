package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;

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
     */
    public boolean extractMethodExtractMethodConflictCell(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        // Extract Method/Extract Method overlapping fragments conflict
        if(checkOverlappingFragmentsConflict(dispatcherObject, receiverObject)) {
            return true;
        }
        // Extract Method/Extract Method accidental override conflict
        else if(checkOverrideConflict(dispatcherObject, receiverObject)) {
            return true;
        }
        // Extract Method/Extract Method accidental override conflict
        else if(checkOverloadConflict(dispatcherObject, receiverObject)) {
            return true;
        }
        // Extract Method/Extract Method naming conflict
        return checkMethodNamingConflict(dispatcherObject, receiverObject);
    }

    /*
     * An overlapping fragments conflict will occur if the source method of both extract method refactorings is the same
     * and the extracted fragments overlap. The extracted fragments overlap if a code fragment in the source method is
     * extracted to both extracted methods.
     */
    public boolean checkOverlappingFragmentsConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        ExtractMethodObject dispatcherExtractMethod = (ExtractMethodObject) dispatcherObject;
        ExtractMethodObject receiverExtractMethod = (ExtractMethodObject) receiverObject;

        // Get the refactored operations
        MethodSignatureObject dispatcherOriginalMethod = dispatcherExtractMethod.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverExtractMethod.getOriginalMethodSignature();

        String dispatcherOriginalClassName = dispatcherExtractMethod.getOriginalClassName();
        String receiverOriginalClassName = receiverExtractMethod.getOriginalClassName();

        // If the source methods are in different classes or are not the same method, return false
        if(!dispatcherOriginalClassName.equals(receiverOriginalClassName)
                && !dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            return false;
        }

        Set<AbstractCodeFragment> dispatcherFragments = dispatcherExtractMethod.getExtractedCodeFragments();
        Set<AbstractCodeFragment> receiverFragments = receiverExtractMethod.getExtractedCodeFragments();
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
    public boolean checkOverrideConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        ExtractMethodObject dispatcherExtractMethod = (ExtractMethodObject) dispatcherObject;
        ExtractMethodObject receiverExtractMethod = (ExtractMethodObject) receiverObject;

        // Get the refactored operations
        MethodSignatureObject dispatcherDestinationMethod = dispatcherExtractMethod.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverExtractMethod.getDestinationMethodSignature();
        // Get the class names
        String dispatcherOriginalClassName = dispatcherExtractMethod.getOriginalClassName();
        String receiverOriginalClassName = receiverExtractMethod.getOriginalClassName();

        // If the extract methods happen in the same class then there is no override conflict
        if(dispatcherOriginalClassName.equals(receiverOriginalClassName)) {
            return false;
        }
        String dispatcherOriginalFile = dispatcherExtractMethod.getOriginalFilePath();
        String receiverOriginalFile = receiverExtractMethod.getOriginalFilePath();
        Utils utils = new Utils(project);
        PsiClass dispatcherPsiClass = utils.getPsiClassByFilePath(dispatcherOriginalFile, dispatcherOriginalClassName);
        PsiClass receiverPsiClass = utils.getPsiClassByFilePath(receiverOriginalFile, receiverOriginalClassName);
        if(!ifClassExtends(dispatcherPsiClass, receiverPsiClass)) {
            return false;
        }

        return dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }

    /*
     * An overload conflict will occur between two extract method refactorings if the extracted methods have the same name and
     * different signatures. They need to either be in the same class, or in two classes that have an inheritance relationship.
     */
    public boolean checkOverloadConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        ExtractMethodObject dispatcherExtractMethod = (ExtractMethodObject) dispatcherObject;
        ExtractMethodObject receiverExtractMethod = (ExtractMethodObject) receiverObject;

        MethodSignatureObject dispatcherDestinationMethod = dispatcherExtractMethod.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverExtractMethod.getDestinationMethodSignature();
        // Get class names
        String dispatcherOriginalClassName = dispatcherExtractMethod.getOriginalClassName();
        String receiverOriginalClassName = receiverExtractMethod.getOriginalClassName();
        // If the methods are in different classes, check if one class inherits the other
        if (!dispatcherOriginalClassName.equals(receiverOriginalClassName)) {
            Utils utils = new Utils(project);
            String dispatcherOriginalFile = dispatcherExtractMethod.getOriginalFilePath();
            String receiverOriginalFile = receiverExtractMethod.getOriginalFilePath();
            PsiClass dispatcherPsiClass = utils.getPsiClassByFilePath(dispatcherOriginalFile, dispatcherOriginalClassName);
            PsiClass receiverPsiClass = utils.getPsiClassByFilePath(receiverOriginalFile, receiverOriginalClassName);
            if(!ifClassExtends(dispatcherPsiClass, receiverPsiClass)) {
                return false;
            }
        }
        String dispatcherMethodName = dispatcherDestinationMethod.getName();
        String receiverMethodName = receiverDestinationMethod.getName();

        // If the signatures are equal, then it is not an accidental overload
        if(dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod)) {
            return false;
        }
        // If the signatures are different and the names are the same, it is an accidental overload
        return dispatcherMethodName.equals(receiverMethodName);
    }

    /*
     * A naming conflict will occur between two extract method refactorings if the extracted methods exist in the same class
     * and they have the same signature.
     */
    public boolean checkMethodNamingConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        ExtractMethodObject dispatcherExtractMethod = (ExtractMethodObject) dispatcherObject;
        ExtractMethodObject receiverExtractMethod = (ExtractMethodObject) receiverObject;

        MethodSignatureObject dispatcherDestinationMethod = dispatcherExtractMethod.getDestinationMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverExtractMethod.getDestinationMethodSignature();

        String dispatcherOriginalClassName = dispatcherExtractMethod.getOriginalClassName();
        String receiverOriginalClassName = receiverExtractMethod.getOriginalClassName();

        // If the methods are in different classes
        if (!dispatcherOriginalClassName.equals(receiverOriginalClassName)) {
            return false;
        }
        return dispatcherDestinationMethod.equalsSignature(receiverDestinationMethod);
    }


}
