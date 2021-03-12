package ca.ualberta.cs.smr.core.matrix.conflictCheckers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.UMLOperation;
import org.refactoringminer.api.Refactoring;



import static ca.ualberta.cs.smr.utils.MatrixUtils.*;


public class ConflictCheckers {
    Project project;


    public ConflictCheckers(Project project) {
        this.project = project;
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
        String elementClassName = elementRefactoredOperation.getClassName();
        String visitorClassName = visitorRefactoredOperation.getClassName();

        // If the rename methods happen in the same class then there is no override conflict
        if(isSameName(elementClassName, visitorClassName)) {
            return false;
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
        String elementClassName = elementOriginalOperation.getClassName();
        String visitorClassName = visitorOriginalOperation.getClassName();
        // If the methods are in different classes, no overloading happens
        if (!isSameName(elementClassName, visitorClassName)) {
            return false;
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
        String elementClassName = elementRef.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        String visitorClassName = visitorRef.getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        if(visitorNode.isDependent()) {
            Node visitorHead = visitorNode.getHeadOfDependenceChain();
            visitorClassName = visitorHead.getRefactoring().getInvolvedClassesBeforeRefactoring().iterator().next().getRight();

        }

        if(elementNode.isDependent()) {
            Node elementHead = elementNode.getHeadOfDependenceChain();
            elementClassName = elementHead.getRefactoring().getInvolvedClassesBeforeRefactoring().iterator().next().getRight();
        }
        // Get class names
//        String elementClassName = getOriginalRenameOperationClassName(elementRef);
//        String visitorClassName = getOriginalRenameOperationClassName(visitorRef);

        // If the methods are in different classes
        if (!isSameName(elementClassName, visitorClassName)) {
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

    public boolean checkClassNamingConflict(Node elementNode, Node visitorNode) {
        Refactoring elementRef = elementNode.getRefactoring();
        Refactoring visitorRef = visitorNode.getRefactoring();
        // Get the package for each class
        String elementPackage = getOriginalClassPackage(elementRef);
        String visitorPackage = getOriginalClassPackage(visitorRef);
        // Check that the classes are in the same package
        if(!isSameName(elementPackage, visitorPackage)) {
            return false;
        }
        String elementOriginalClassName = getOriginalClassOperationName(elementRef);
        String visitorOriginalClassName = getOriginalClassOperationName(visitorRef);
        String elementNewClassName = getRefactoredClassOperationName(elementRef);
        String visitorNewClassName = getRefactoredClassOperationName(visitorRef);

        return checkNamingConflict(elementOriginalClassName, visitorOriginalClassName,
                                                elementNewClassName, visitorNewClassName);
    }

    public boolean checkNamingConflict(String elementOriginal, String visitorOriginal, String elementNew,
                                                    String visitorNew) {
        // If the original method names are equal but the destination names are not equal, check for conflict
        if(isSameName(elementOriginal, visitorOriginal) && !isSameName(elementNew, visitorNew)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else return !isSameName(elementOriginal, visitorOriginal) && isSameName(elementNew, visitorNew);
    }

}


