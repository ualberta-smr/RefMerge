package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;
import static ca.ualberta.cs.smr.utils.MatrixUtils.getRefactoredMethodName;

/*
 * Contains the logic checks for rename method/rename method conflicts.
 */
public class RenameMethodRenameMethodCell {
    final private Project project;

    public RenameMethodRenameMethodCell(Project project) {
        this.project = project;
    }

    /*
     *  Check if a rename method refactoring conflicts with a second rename method refactoring. Rename method/rename method
     *  can result in an override conflict, an overload conflict, or a naming conflict.
     *  @param dispatcherNode: A node containing the dispatcher rename method refactoring.
     *  @param receiverNode: A node containing the receiver rename method refactoring.
     */
    public boolean renameMethodRenameMethodConflictCell(Node dispatcherNode, Node receiverNode) {
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        // Check for a method override conflict
        if(renameMethodRenameMethodCell.checkOverrideConflict(dispatcherNode, receiverNode)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(renameMethodRenameMethodCell.checkOverloadConflict(dispatcherNode, receiverNode)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(renameMethodRenameMethodCell.checkMethodNamingConflict(dispatcherNode, receiverNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    public boolean checkOverrideConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRef = dispatcherNode.getRefactoring();
        Refactoring receiverRef = receiverNode.getRefactoring();
        // Get the original operations
        UMLOperation dispatcherOriginalOperation = getOriginalRenameOperation(dispatcherRef);
        UMLOperation receiverOriginalOperation = getOriginalRenameOperation(receiverRef);
        // Get the refactored operations
        UMLOperation dispatcherRefactoredOperation = getRefactoredRenameOperation(dispatcherRef);
        UMLOperation receiverRefactoredOperation = getRefactoredRenameOperation(receiverRef);
        // Get the class names
        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
        String receiverClassName = receiverNode.getDependenceChainClassHead();

        // If the rename methods happen in the same class then there is no override conflict
        if(isSameName(dispatcherClassName, receiverClassName)) {
            if(isSameOriginalClass(dispatcherNode, receiverNode)) {
                return false;
            }
        }
        Utils utils = new Utils(project);
        String dispatcherFile = dispatcherRefactoredOperation.getLocationInfo().getFilePath();
        String receiverFile = receiverRefactoredOperation.getLocationInfo().getFilePath();
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
        if(!ifClassExtends(psiDispatcher, psiReceiver)) {
            return false;
        }
        // Get original method names
        String dispatcherOriginalMethodName = dispatcherOriginalOperation.getName();
        String receiverOriginalMethodName = receiverOriginalOperation.getName();
        // get new method names
        String dispatcherNewMethodName = dispatcherRefactoredOperation.getName();
        String receiverNewMethodName = receiverRefactoredOperation.getName();
        // Check if the methods start with the same name and end with different names, or if they end with the same name
        // and start with different names. If they do, then there's a likely override conflict.
        return !isSameName(dispatcherOriginalMethodName, receiverOriginalMethodName) &&
                isSameName(dispatcherNewMethodName, receiverNewMethodName) &&
                dispatcherRefactoredOperation.equalSignature(receiverRefactoredOperation);
    }

    public boolean checkOverloadConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRef = dispatcherNode.getRefactoring();
        Refactoring receiverRef = receiverNode.getRefactoring();
        // Get the original operations
        UMLOperation dispatcherOriginalOperation = getOriginalRenameOperation(dispatcherRef);
        UMLOperation receiverOriginalOperation = getOriginalRenameOperation(receiverRef);
        // Get the refactored operations
        UMLOperation dispatcherRefactoredOperation = getRefactoredRenameOperation(dispatcherRef);
        UMLOperation receiverRefactoredOperation = getRefactoredRenameOperation(receiverRef);
        // Get class names
        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
        String receiverClassName = receiverNode.getDependenceChainClassHead();
        // If the methods are in different classes, no overloading happens
        if (!isSameName(dispatcherClassName, receiverClassName)) {
            Utils utils = new Utils(project);
            String dispatcherFile = dispatcherRefactoredOperation.getLocationInfo().getFilePath();
            String receiverFile = receiverRefactoredOperation.getLocationInfo().getFilePath();
            PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, dispatcherClassName);
            PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, receiverClassName);
            if(!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }
        // Get original method names
        String dispatcherOriginalMethodName = dispatcherOriginalOperation.getName();
        String receiverOriginalMethodName = receiverOriginalOperation.getName();
        // Get new method names
        String dispatcherNewMethodName = dispatcherRefactoredOperation.getName();
        String receiverNewMethodName = receiverRefactoredOperation.getName();


        // If two methods with different signatures are renamed to the same method, overloading conflict
        return !isSameName(dispatcherOriginalMethodName, receiverOriginalMethodName) &&
                isSameName(dispatcherNewMethodName, receiverNewMethodName) &&
                !dispatcherRefactoredOperation.equalParameters(receiverOriginalOperation);
    }

    public boolean checkMethodNamingConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRef = dispatcherNode.getRefactoring();
        Refactoring receiverRef = receiverNode.getRefactoring();
//        String dispatcherClassName = dispatcherNode.getDependenceChainClassHead();
//        String receiverClassName = receiverNode.getDependenceChainClassHead();
        String dispatcherClassName = ((RenameOperationRefactoring) dispatcherRef).getOriginalOperation().getClassName();
        String receiverClassName = ((RenameOperationRefactoring) receiverRef).getOriginalOperation().getClassName();

        // If the methods are in different classes
        if (!isSameName(dispatcherClassName, receiverClassName)) {
            if(!isSameOriginalClass(dispatcherNode, receiverNode))
                return false;
        }
        // Get original method names
        String dispatcherOriginalName = getOriginalMethodName(dispatcherRef);
        String receiverOriginalName = getOriginalMethodName(receiverRef);
        // get new method names
        String dispatcherNewName = getRefactoredMethodName(dispatcherRef);
        String receiverNewName = getRefactoredMethodName(receiverRef);
        // Check naming conflict
        return checkNamingConflict(dispatcherOriginalName, receiverOriginalName, dispatcherNewName, receiverNewName);
    }

}
