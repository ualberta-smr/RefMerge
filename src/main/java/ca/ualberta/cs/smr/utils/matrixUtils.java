package ca.ualberta.cs.smr.utils;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class matrixUtils {
    static public boolean isSameName(String elementName, String visitorName) {
        return elementName.equals(visitorName);
    }

    static public boolean ifClassExtends(Class<?> elementClass, Class<?> visitorClass) {
        return elementClass.isAssignableFrom(visitorClass) || visitorClass.isAssignableFrom(elementClass);
    }

    static public UMLOperation getOriginalRenameOperation(Refactoring ref) {
        return ((RenameOperationRefactoring) ref).getOriginalOperation();
    }

    static public UMLOperation getRefactoredRenameOperation(Refactoring ref) {
        return ((RenameOperationRefactoring) ref).getRenamedOperation();
    }

    static public String getOriginalMethodName(Refactoring ref) {
        return getOriginalRenameOperation(ref).getName();
    }

    static public String getRefactoredMethodName(Refactoring ref) {
        return getRefactoredRenameOperation(ref).getName();
    }

    static public String getOriginalRenameOperationClassName(Refactoring ref) {
        return getOriginalRenameOperation(ref).getClassName();
    }

    static public UMLClass getOriginalClassOperation(Refactoring ref) {
        return ((RenameClassRefactoring) ref).getOriginalClass();
    }

    static public UMLClass getRefactoredClassOperation(Refactoring ref) {
        return ((RenameClassRefactoring) ref).getRenamedClass();
    }

    static public String getOriginalClassPackage(Refactoring ref) {
        return getOriginalClassOperation(ref).getPackageName();
    }

    static public String getOriginalClassOperationName(Refactoring ref) {
        return getOriginalClassOperation(ref).getName();
    }

    static public String getRefactoredClassOperationName(Refactoring ref) {
        return getRefactoredClassOperation(ref).getName();
    }
}
