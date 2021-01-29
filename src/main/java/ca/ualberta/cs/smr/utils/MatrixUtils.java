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
        UMLClass umlClass = null;
        try {
            model = new UMLModelASTReader(new File(path)).getUmlModel();
            List<UMLClass> umlClasses = model.getClassList();
            for(UMLClass uml : umlClasses) {
                if(uml.getName().equals(name)) {
                    return uml;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return umlClass;
    }


    static public boolean ifClassExtends(UMLClass elementClass, UMLClass visitorClass) {
        UMLType elementSuperClassType = elementClass.getSuperclass();
        UMLType visitorSuperClassType = visitorClass.getSuperclass();
        if(elementSuperClassType == null && visitorSuperClassType == null) {
            return false;
        }
        if(elementSuperClassType != null) {
            String elementSuperClassName = elementSuperClassType.getClassType();
            String visitorClassName = visitorClass.getName();
            if(visitorClassName.equals(elementSuperClassName)) {
                return true;
            }
        }
        else if(visitorSuperClassType != null) {
            String visitorSuperClassName = visitorSuperClassType.getClassType();
            String elementClassName = elementClass.getName();
            if(elementClassName.equals(visitorSuperClassName)) {
                return true;
            }
        }
        return false;
    }


}
