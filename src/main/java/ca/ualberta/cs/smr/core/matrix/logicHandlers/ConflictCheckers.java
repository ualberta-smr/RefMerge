package ca.ualberta.cs.smr.core.matrix.logicHandlers;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.UMLOperation;
import org.refactoringminer.api.Refactoring;


import static ca.ualberta.cs.smr.core.matrix.logicHandlers.LogicHandlers.ifClassExtends;
import static ca.ualberta.cs.smr.core.matrix.logicHandlers.LogicHandlers.isSameName;


public class ConflictCheckers {


    static public boolean checkOverrideConflict(Refactoring elementRef, Refactoring visitorRef) {
        // Get the original operations
        UMLOperation elementOriginalOperation = ((RenameOperationRefactoring) elementRef).getOriginalOperation();
        UMLOperation visitorOriginalOperation = ((RenameOperationRefactoring) visitorRef).getOriginalOperation();
        // Get the refactored operations
        UMLOperation elementRefactoredOperation = ((RenameOperationRefactoring) elementRef).getRenamedOperation();
        UMLOperation visitorRefactoredOperation = ((RenameOperationRefactoring) visitorRef).getRenamedOperation();
        // Get the class names
        String elementClassName = elementRefactoredOperation.getClassName();
        String visitorClassName = visitorRefactoredOperation.getClassName();

        // If the rename methods happen in the same class then there is no override conflict
        if(isSameName(elementClassName, visitorClassName)) {
            return false;
        }
        // Get the class of each method
        Class elementClass = elementOriginalOperation.getClass();
        Class visitorClass = visitorOriginalOperation.getClass();
        // If neither class extends each other then there is no override conflict
        if(!ifClassExtends(elementClass, visitorClass)) {
            return false;
        }
        // Get original method names
        String elementOriginalMethodName = elementOriginalOperation.getName();
        String visitorOriginalMethodName = visitorOriginalOperation.getName();
        // get new method names
        String elementNewMethodName = elementRefactoredOperation.getName();
        String visitorNewMethodName = visitorRefactoredOperation.getName();
        // If the methods start with the same name and end with the same names, then there is no override conflict
        if(isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                                                isSameName(elementNewMethodName, visitorNewMethodName)) {
            return false;
        }
        // Otherwise if the methods start with different names and end with different names, there is no override conflict
        else if(!isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                                                !isSameName(elementNewMethodName, visitorNewMethodName)) {
            return false;
        }
        // If the method starts with the same name and has two different names after the refactoring, or two methods
        // are renamed to the same name, there is a conflict
        return true;
    }

    static public boolean checkOverloadConflict(Refactoring elementRef, Refactoring visitorRef) {
        // Get the original operations
        UMLOperation elementOriginalOperation = ((RenameOperationRefactoring) elementRef).getOriginalOperation();
        UMLOperation visitorOriginalOperation = ((RenameOperationRefactoring) visitorRef).getOriginalOperation();
        // Get the refactored operations
        UMLOperation elementRefactoredOperation = ((RenameOperationRefactoring) elementRef).getRenamedOperation();
        UMLOperation visitorRefactoredOperation = ((RenameOperationRefactoring) visitorRef).getRenamedOperation();
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

        // If the methods have the same name but different signatures, and the methods are refactored to two different
        // names, there is an overloading conflict
        if (isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                !isSameName(elementNewMethodName, visitorNewMethodName) &&
                !elementOriginalOperation.equalSignature(visitorOriginalOperation)) {
            return true;
        }
        // If two methods with different signatures are renamed to the same method, overloading conflict
        else if(!isSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                isSameName(elementNewMethodName, visitorNewMethodName) &&
                !elementRefactoredOperation.equalSignature(visitorOriginalOperation)) {
            return true;
        }
        return false;
    }

    static public boolean checkNamingConflict(Refactoring elementRef, Refactoring visitorRef) {
        // Get the original operations
        UMLOperation elementOriginalOperation = ((RenameOperationRefactoring) elementRef).getOriginalOperation();
        UMLOperation visitorOriginalOperation = ((RenameOperationRefactoring) visitorRef).getOriginalOperation();
        // Get the refactored operations
        UMLOperation elementRefactoredOperation = ((RenameOperationRefactoring) elementRef).getRenamedOperation();
        UMLOperation visitorRefactoredOperation = ((RenameOperationRefactoring) visitorRef).getRenamedOperation();
        // Get class names
        String elementClassName = elementOriginalOperation.getClassName();
        String visitorClassName = visitorOriginalOperation.getClassName();

        // If the methods are in different classes
        if (!isSameName(elementClassName, visitorClassName)) {
            return false;
        }
        // Get original method names
        String elementOriginalName = elementOriginalOperation.getName();
        String visitorOriginalName = visitorOriginalOperation.getName();
        // get new method names
        String elementNewName = elementRefactoredOperation.getName();
        String visitorNewName = visitorRefactoredOperation.getName();
        // If the original method names are equal but the destination names are not equal, check for conflict
        if(isSameName(elementOriginalName,visitorOriginalName) && !isSameName(elementNewName, visitorNewName)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else if(!isSameName(elementOriginalName, visitorOriginalName) && isSameName(elementNewName, visitorNewName)) {
            return true;
        }
        return false;
    }
}


