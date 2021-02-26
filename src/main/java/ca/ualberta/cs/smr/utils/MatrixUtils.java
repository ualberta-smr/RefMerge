package ca.ualberta.cs.smr.utils;


import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.*;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MatrixUtils {
    static public boolean isSameName(String elementName, String visitorName) {
        return elementName.equals(visitorName);
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

    static public UMLClass getUMLClass(String name, String path) {
        UMLModel model;
        try {
            model = new UMLModelASTReader(new File(path)).getUmlModel();
            List<UMLClass> umlClasses = model.getClassList();
            for(UMLClass umlClass : umlClasses) {
                if(umlClass.getName().equals(name)) {
                    return umlClass;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public boolean ifClassExtends(PsiClass element, PsiClass visitor) {
        if(element.isInheritor(visitor, true)) {
            return true;
        }
        else return visitor.isInheritor(element, true);
    }

}
