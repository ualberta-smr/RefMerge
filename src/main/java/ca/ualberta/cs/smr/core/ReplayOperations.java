package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;


public class ReplayOperations {

    Project project;

    public ReplayOperations(Project proj) {
        this.project = proj;
    }

    /*
     * replayRenameMethod performs the rename method refactoring.
     */
    public void replayRenameMethod(Refactoring ref) {
        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        String destName = renamed.getName();
        String qualifiedClass = renamed.getClassName();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(project);
        // Get the PSI class using the qualified class name
        PsiClass psiClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(project));
        // If the PSI class is null, then this part of the project wasn't built and we need to find the PSI class
        // another way
        if(psiClass == null) {
            Utils utils = new Utils(project);
            String filePath = renamed.getLocationInfo().getFilePath();
            psiClass = utils.getPsiClassByFilePath(filePath, qualifiedClass);
        }
        assert psiClass != null;
        PsiMethod method = Utils.getPsiMethod(psiClass, original);
        assert method != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(method, destName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
        // Update the virtual file containing the refactoring
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
    }


    public void replayRenameClass(Refactoring ref) {

        UMLClass original = ((RenameClassRefactoring) ref).getOriginalClass();
        UMLClass renamed = ((RenameClassRefactoring) ref).getRenamedClass();
        String srcQualifiedClass = original.getName();
        String destQualifiedClass = renamed.getName();
        String destClassName = destQualifiedClass.substring(destQualifiedClass.lastIndexOf(".") + 1);
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(project);
        PsiClass psiClass = jPF.findClass(srcQualifiedClass, GlobalSearchScope.allScope((project)));
        // If the class isn't found, there might not have been a gradle file and we need to find the class another way
        if(psiClass == null) {
            Utils utils = new Utils(project);
            String filePath = original.getLocationInfo().getFilePath();
            psiClass = utils.getPsiClassByFilePath(filePath, srcQualifiedClass);
        }
        assert psiClass != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(psiClass, destClassName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

}
