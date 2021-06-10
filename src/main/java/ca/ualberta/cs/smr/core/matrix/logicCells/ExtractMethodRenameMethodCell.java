package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

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
     */
    public boolean extractMethodRenameMethodConflictCell(RefactoringObject renameMethod, RefactoringObject extractMethod) {
        // Extract Method/Rename Method override conflict
        if(checkOverrideConflict(renameMethod, extractMethod)) {
            return true;
        }
        // Extract Method/Rename Method overload conflict
        else if(checkOverloadConflict(renameMethod, extractMethod)) {
            return true;
        }

        // Extract Method/Rename Method naming conflict
        else return checkMethodNamingConflict(renameMethod, extractMethod);
    }

    /*
     *  Check if an ordering dependence exists between extract method/rename method refactorings.
     */
    public boolean extractMethodRenameMethodDependenceCell(RefactoringObject renameMethod, RefactoringObject extractMethod) {
        return checkExtractMethodRenameMethodDependence(renameMethod, extractMethod);
    }


    /*
     * Check if the extracted method and the renamed method cause an accidental override
     */
    public boolean checkOverrideConflict(RefactoringObject renameMethod, RefactoringObject extractMethod) {
        RenameMethodObject renameMethodObject = (RenameMethodObject) renameMethod;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;

        // Get the refactored operations
        MethodSignatureObject renameDestinationMethod = renameMethodObject.getDestinationMethodSignature();
        MethodSignatureObject extractDestinationMethod = extractMethodObject.getDestinationMethodSignature();
        // Get the class names
        String renameDestinationClassName = renameMethodObject.getDestinationClassName();
        String extractDestinationClassName = extractMethodObject.getDestinationClassName();

        // If the rename methods happen in the same class then there is no override conflict
        if(renameDestinationClassName.equals(extractDestinationClassName)) {
            return false;
        }
        String renameDestinationFile = renameMethodObject.getDestinationFilePath();
        String extractDestinationFile = extractMethodObject.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass renameMethodPsiClass = utils.getPsiClassByFilePath(renameDestinationFile, renameDestinationClassName);
        PsiClass extractMethodPsiClass = utils.getPsiClassByFilePath(extractDestinationFile, extractDestinationClassName);
        if(!ifClassExtends(renameMethodPsiClass, extractMethodPsiClass)) {
            return false;
        }

        // If they have different signatures then there cannot be overriding
        return renameDestinationMethod.equalsSignature(extractDestinationMethod);
    }

    /*
     * Check if the extracted method and renamed method cause an accidental overload
     */
    public boolean checkOverloadConflict(RefactoringObject renameMethod, RefactoringObject extractMethod) {
        RenameMethodObject renameMethodObject = (RenameMethodObject) renameMethod;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;

        MethodSignatureObject renameDestinationMethod = renameMethodObject.getDestinationMethodSignature();
        MethodSignatureObject extractDestinationMethod = extractMethodObject.getDestinationMethodSignature();

        String renameDestinationClassName = renameMethodObject.getDestinationClassName();
        String extractDestinationClassName = extractMethodObject.getDestinationClassName();
        // If the methods are in different classes, check if one class inherits the other
        if (!renameDestinationClassName.equals(extractDestinationClassName)) {
            Utils utils = new Utils(project);
            String renameMethodFile = renameMethodObject.getDestinationFilePath();
            String extractMethodFile = extractMethodObject.getDestinationFilePath();
            PsiClass renameMethodPsiClass = utils.getPsiClassByFilePath(renameMethodFile, renameDestinationClassName);
            PsiClass extractMethodPsiClass = utils.getPsiClassByFilePath(extractMethodFile, extractDestinationClassName);
            if(!ifClassExtends(renameMethodPsiClass, extractMethodPsiClass)) {
                return false;
            }
        }
        String renameDestinationName = renameDestinationMethod.getName();
        String extractDestinationName = extractDestinationMethod.getName();

        // If the signatures are equal, then it is not an accidental overload
        if(renameDestinationMethod.equalsSignature(extractDestinationMethod)) {
            return false;
        }
        // If the signatures are different and the names are the same, it is an accidental overload
        return renameDestinationName.equals(extractDestinationName);
    }

    /*
     * Check if the extracted method has the same name as the renamed method
     */
    public boolean checkMethodNamingConflict(RefactoringObject renameMethod, RefactoringObject extractMethod) {
        RenameMethodObject renameMethodObject = (RenameMethodObject) renameMethod;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        MethodSignatureObject renameDestinationMethod = renameMethodObject.getDestinationMethodSignature();
        MethodSignatureObject extractDestinationMethod = extractMethodObject.getDestinationMethodSignature();

        String renameDestinationClassName = renameMethodObject.getDestinationClassName();
        String extractDestinationClassName = extractMethodObject.getDestinationClassName();

        // If the methods are in different classes
        if (!renameDestinationClassName.equals(extractDestinationClassName)) {
                return false;
        }
        return renameDestinationMethod.equalsSignature(extractDestinationMethod);
    }

    /*
     * Check if the method needs to be extracted before the source method is renamed
     */
    public static boolean checkExtractMethodRenameMethodDependence(RefactoringObject renameMethod,
                                                                   RefactoringObject extractMethod) {
        RenameMethodObject renameMethodObject = (RenameMethodObject) renameMethod;
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        MethodSignatureObject renameOriginalMethod = renameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject extractOriginalMethod = extractMethodObject.getOriginalMethodSignature();

        String renameOriginalClassName = renameMethodObject.getOriginalClassName();
        String extractOriginalClassName = extractMethodObject.getOriginalClassName();

        // If the methods are in different classes then there cannot be ordering dependence
        if (!renameOriginalClassName.equals(extractOriginalClassName)) {
                return false;
        }

        // If the signatures are the same and they are in the same class, then the source method and original method
        // must be the same
        return renameOriginalMethod.equalsSignature(extractOriginalMethod);
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
        MethodSignatureObject originalRenameMethod = renameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject destinationRenameMethod = renameMethodObject.getDestinationMethodSignature();

        ExtractMethodObject extractMethodObject = (ExtractMethodObject) extractMethod;
        String originalExtractClassName = extractMethodObject.getOriginalClassName();
        String destinationExtractClassName = extractMethodObject.getDestinationClassName();
        MethodSignatureObject originalExtractMethod = extractMethodObject.getOriginalMethodSignature();
        MethodSignatureObject destinationExtractMethod = extractMethodObject.getDestinationMethodSignature();

        // If the source method and destination method are the same in extract method and rename method, then the
        // extracted method was actually extracted from the original method in the rename method refactoring (for conflict
        // checks). Update the source method details to use the original method.
        if(destinationRenameMethod.equalsSignature(originalExtractMethod) && (destinationRenameClassName.equals(originalExtractClassName)
        || originalRenameClassName.equals(originalExtractClassName))) {
            extractMethod.setOriginalFilePath(renameMethodObject.getOriginalFilePath());
            ((ExtractMethodObject) extractMethod).setOriginalClassName(renameMethodObject.getOriginalClassName());
            ((ExtractMethodObject) extractMethod).setOriginalMethodSignature(originalRenameMethod);
        }
        // If the original name of the rename method and the extracted method name are the same, then the method was extracted
        // and then renamed. This is a transitive refactoring so we update the extract method refactoring with the new name
        // of the extracted method.
        else if(originalRenameMethod.equalsSignature(destinationExtractMethod) && (destinationRenameClassName.equals(destinationExtractClassName)
        || originalRenameClassName.equals(originalExtractClassName))) {
            isTransitive = true;
            extractMethod.setDestinationFilePath(renameMethodObject.getDestinationFilePath());
            ((ExtractMethodObject) extractMethod).setDestinationClassName(renameMethodObject.getDestinationClassName());
            ((ExtractMethodObject) extractMethod).setDestinationMethodSignature(destinationRenameMethod);
        }
        // If the original class name

        return isTransitive;
    }
}
