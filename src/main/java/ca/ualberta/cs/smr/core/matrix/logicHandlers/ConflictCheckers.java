package ca.ualberta.cs.smr.core.matrix.logicHandlers;

import gr.uom.java.xmi.UMLOperation;
import org.refactoringminer.api.Refactoring;


import static ca.ualberta.cs.smr.utils.MatrixUtils.*;


public class ConflictCheckers {


    static public boolean checkOverrideConflict(Refactoring elementRef, Refactoring visitorRef) {
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
        // Get the class of each method
        Class<?> elementClass = elementOriginalOperation.getClass();
        Class<?> visitorClass = visitorOriginalOperation.getClass();
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

    static public boolean checkMethodNamingConflict(Refactoring elementRef, Refactoring visitorRef) {
        // Get class names
        String elementClassName = getOriginalRenameOperationClassName(elementRef);
        String visitorClassName = getOriginalRenameOperationClassName(visitorRef);

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

    static public boolean checkClassNamingConflict(Refactoring elementRef, Refactoring visitorRef) {
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

    static public boolean checkNamingConflict(String elementOriginal, String visitorOriginal, String elementNew,
                                                    String visitorNew) {
        // If the original method names are equal but the destination names are not equal, check for conflict
        if(isSameName(elementOriginal, visitorOriginal) && !isSameName(elementNew, visitorNew)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else if(!isSameName(elementOriginal, visitorOriginal) && isSameName(elementNew, visitorNew)) {
            return true;
        }
        return false;
    }

}


