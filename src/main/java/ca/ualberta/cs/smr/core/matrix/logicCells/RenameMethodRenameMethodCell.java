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

public class RenameMethodRenameMethodCell {
    final private Project project;

    public RenameMethodRenameMethodCell(Project project) {
        this.project = project;
    }

    /*
     *  Check if a rename method refactoring conflicts with a second rename method refactoring.
     */
    public boolean renameMethodRenameMethodConflictCell(Node elementNode, Node visitorNode) {
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        // Check for a method override conflict
        if(renameMethodRenameMethodCell.checkOverrideConflict(elementNode, visitorNode)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(renameMethodRenameMethodCell.checkOverloadConflict(elementNode, visitorNode)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(renameMethodRenameMethodCell.checkMethodNamingConflict(elementNode, visitorNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    public boolean checkOverrideConflict(Node elementNode, Node visitorNode) {
        Refactoring elementRef = elementNode.getRefactoring();
        Refactoring visitorRef = visitorNode.getRefactoring();
        // Get the original operations
        UMLOperation elementOriginalOperation = getOriginalRenameOperation(elementRef);
        UMLOperation visitorOriginalOperation = getOriginalRenameOperation(visitorRef);
        // Get the refactored operations
        UMLOperation elementRefactoredOperation = getRefactoredRenameOperation(elementRef);
        UMLOperation visitorRefactoredOperation = getRefactoredRenameOperation(visitorRef);
        // Get the class names
        String elementClassName = elementNode.getDependenceChainClassHead();
        String visitorClassName = visitorNode.getDependenceChainClassHead();

        // If the rename methods happen in the same class then there is no override conflict
        if(isSameName(elementClassName, visitorClassName)) {
            if(isSameOriginalClass(elementNode, visitorNode)) {
                return false;
            }
        }
        Utils utils = new Utils(project);
        String elementFile = elementRefactoredOperation.getLocationInfo().getFilePath();
        String visitorFile = visitorRefactoredOperation.getLocationInfo().getFilePath();
        PsiClass psiElement = utils.getPsiClassByFilePath(elementFile, elementClassName);
        PsiClass psiVisitor = utils.getPsiClassByFilePath(visitorFile, visitorClassName);
        if(!ifClassExtends(psiElement, psiVisitor)) {
            return false;
        }
        // Get original method names
        String elementOriginalMethodName = elementOriginalOperation.getName();
        String visitorOriginalMethodName = visitorOriginalOperation.getName();
        // get new method names
        String elementNewMethodName = elementRefactoredOperation.getName();
        String visitorNewMethodName = visitorRefactoredOperation.getName();
        // Check if the methods start with the same name and end with different names, or if they end with the same name
        // and start with different names. If they do, then there's a likely override conflict.
        return !isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                isSameName(elementNewMethodName, visitorNewMethodName) &&
                elementRefactoredOperation.equalSignature(visitorRefactoredOperation);
    }

    public boolean checkOverloadConflict(Node elementNode, Node visitorNode) {
        Refactoring elementRef = elementNode.getRefactoring();
        Refactoring visitorRef = visitorNode.getRefactoring();
        // Get the original operations
        UMLOperation elementOriginalOperation = getOriginalRenameOperation(elementRef);
        UMLOperation visitorOriginalOperation = getOriginalRenameOperation(visitorRef);
        // Get the refactored operations
        UMLOperation elementRefactoredOperation = getRefactoredRenameOperation(elementRef);
        UMLOperation visitorRefactoredOperation = getRefactoredRenameOperation(visitorRef);
        // Get class names
        String elementClassName = elementNode.getDependenceChainClassHead();
        String visitorClassName = visitorNode.getDependenceChainClassHead();
        // If the methods are in different classes, no overloading happens
        if (!isSameName(elementClassName, visitorClassName)) {
            Utils utils = new Utils(project);
            String elementFile = elementRefactoredOperation.getLocationInfo().getFilePath();
            String visitorFile = visitorRefactoredOperation.getLocationInfo().getFilePath();
            PsiClass psiElement = utils.getPsiClassByFilePath(elementFile, elementClassName);
            PsiClass psiVisitor = utils.getPsiClassByFilePath(visitorFile, visitorClassName);
            if(!ifClassExtends(psiElement, psiVisitor)) {
                return false;
            }
        }
        // Get original method names
        String elementOriginalMethodName = elementOriginalOperation.getName();
        String visitorOriginalMethodName = visitorOriginalOperation.getName();
        // Get new method names
        String elementNewMethodName = elementRefactoredOperation.getName();
        String visitorNewMethodName = visitorRefactoredOperation.getName();


        // If two methods with different signatures are renamed to the same method, overloading conflict
        return !isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                isSameName(elementNewMethodName, visitorNewMethodName) &&
                !elementRefactoredOperation.equalParameters(visitorOriginalOperation);
    }

    public boolean checkMethodNamingConflict(Node elementNode, Node visitorNode) {
        Refactoring elementRef = elementNode.getRefactoring();
        Refactoring visitorRef = visitorNode.getRefactoring();
//        String elementClassName = elementNode.getDependenceChainClassHead();
//        String visitorClassName = visitorNode.getDependenceChainClassHead();
        String elementClassName = ((RenameOperationRefactoring) elementRef).getOriginalOperation().getClassName();
        String visitorClassName = ((RenameOperationRefactoring) visitorRef).getOriginalOperation().getClassName();

        // If the methods are in different classes
        if (!isSameName(elementClassName, visitorClassName)) {
            if(!isSameOriginalClass(elementNode, visitorNode))
                return false;
        }
        // Get original method names
        String elementOriginalName = getOriginalMethodName(elementRef);
        String visitorOriginalName = getOriginalMethodName(visitorRef);
        // get new method names
        String elementNewName = getRefactoredMethodName(elementRef);
        String visitorNewName = getRefactoredMethodName(visitorRef);
        // Check naming conflict
        return checkNamingConflict(elementOriginalName, visitorOriginalName, elementNewName, visitorNewName);
    }

}
