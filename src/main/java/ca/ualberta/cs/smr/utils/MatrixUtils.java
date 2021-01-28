package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
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


    static public PsiClass getClass(String qualifiedClass) {
        Project proj = ProjectManager.getInstance().getOpenProjects()[0];
        proj.getProjectFile();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        return jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));


    }

    static public boolean ifClassExtends(PsiClass elementClass, PsiClass visitorClass) {
        String visitorName = visitorClass.getQualifiedName();
        String elementName = elementClass.getQualifiedName();
        String visitorSuper = visitorClass.getSuperClass().getQualifiedName();
        String elementSuper = elementClass.getSuperClass().getQualifiedName();
        if(visitorName.equals(elementSuper) || elementName.equals(visitorSuper)) {
            return true;
        }
        return false;
    }
}
