package ca.ualberta.cs.smr.core.matrix.logicHandlers;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import gr.uom.java.xmi.UMLOperation;
import org.refactoringminer.api.Refactoring;

import ca.ualberta.cs.smr.core.matrix.logicHandlers.LogicHandlers;



public class ConflictCheckers {

    /*
     * Check for an override conflict
     */
    static public boolean checkOverrideConflict(Refactoring elementRef, Refactoring visitorRef) {
        LogicHandlers lh = new LogicHandlers();
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
        if(lh.isTheSameName(elementClassName, visitorClassName)) {
            return false;
        }
        // Get the class of each method
        Class elementClass = elementOriginalOperation.getClass();
        Class visitorClass = visitorOriginalOperation.getClass();
        // If neither class extends each other then there is no override conflict
        if(!lh.ifClassExtends(elementClass, visitorClass)) {
            return false;
        }
        // Get original method names
        String elementOriginalMethodName = elementOriginalOperation.getName();
        String visitorOriginalMethodName = visitorOriginalOperation.getName();
        // get new method names
        String elementNewMethodName = elementRefactoredOperation.getName();
        String visitorNewMethodName = visitorRefactoredOperation.getName();
        // If the methods start with the same name and end with the same names, then there is no override conflict
        if(lh.isTheSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                                                lh.isTheSameName(elementNewMethodName, visitorNewMethodName)) {
            return false;
        }
        // Otherwise if the methods start with different names and end with different names, there is no override conflict
        else if(!lh.isTheSameName(elementOriginalMethodName, visitorOriginalMethodName) &&
                                                !lh.isTheSameName(elementNewMethodName, visitorNewMethodName)) {
            return false;
        }
        // If the method starts with the same name and has two different names after the refactoring, or two methods
        // are renamed to the same name, there is a conflict
        return true;
    }


}


