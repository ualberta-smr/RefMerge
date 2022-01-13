package ca.ualberta.cs.smr.refmerge.utils;


import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.*;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class MatrixUtils {
    static public boolean isSameName(String elementName, String visitorName) {
        return elementName.equals(visitorName);
    }

    static public UMLOperation getOriginalRenameOperation(Refactoring ref) {
        return ((RenameOperationRefactoring) ref).getOriginalOperation();
    }

    static public UMLClass getOriginalClassOperation(Refactoring ref) {
        return ((RenameClassRefactoring) ref).getOriginalClass();
    }

    static public UMLClass getRefactoredClassOperation(Refactoring ref) {
        return ((RenameClassRefactoring) ref).getRenamedClass();
    }

    static public String getOriginalClassOperationName(Refactoring ref) {
        return getOriginalClassOperation(ref).getName();
    }

    static public String getRefactoredClassOperationName(Refactoring ref) {
        return getRefactoredClassOperation(ref).getName();
    }

    static public boolean ifClassExtends(PsiClass element, PsiClass visitor) {
        if(element.isInheritor(visitor, true)) {
            return true;
        }
        else return visitor.isInheritor(element, true);
    }

    public static boolean checkNamingConflict(String elementOriginal, String visitorOriginal, String elementNew,
                                       String visitorNew) {
        // If the original method names are equal but the destination names are not equal, check for conflict
        if(isSameName(elementOriginal, visitorOriginal) && !isSameName(elementNew, visitorNew)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else return !isSameName(elementOriginal, visitorOriginal) && isSameName(elementNew, visitorNew);
    }

}
