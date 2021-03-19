package ca.ualberta.cs.smr.utils;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.*;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    static public boolean isSameOriginalClass(Node node1, Node node2) {
        String node1ClassName = getOriginalClassName(node1);
        String node2ClassName = getOriginalClassName(node2);
        return node1ClassName.equals(node2ClassName);
    }

    static public String getOriginalClassName(Node node) {
        ArrayList<Node> nodes = node.getDependsList();
        UMLOperation umlOperation = getOriginalRenameOperation(node.getRefactoring());
        String className = umlOperation.getClassName();
        if(nodes.isEmpty()) {
            return className;
        }
        for(int i = nodes.size() - 1; i > -1; i--) {
            Node previousNode = nodes.get(i);
            Refactoring refactoring = previousNode.getRefactoring();
            RefactoringType type = refactoring.getRefactoringType();
            if(type == RefactoringType.RENAME_CLASS) {
                String renamedClass = getRefactoredClassOperationName(refactoring);
                if(renamedClass.equals(className)) {
                    className = getOriginalClassOperationName(refactoring);
                }
            }
        }
        return className;
    }

}
