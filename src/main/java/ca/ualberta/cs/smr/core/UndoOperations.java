package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;



public class UndoOperations {

    Project project;

    public UndoOperations(Project project) {
        this.project = project;
    }

    /*
     * Undo the rename method refactoring that was performed in the commit
     */
    public void undoRenameMethod(Refactoring ref) {
        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        // Get the original method name
        String srcName = original.getName();
        // Get the refactored method name
        String qualifiedClass = original.getClassName();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(project);
        // get the PSI class using the qualified class name
        PsiClass psiClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(project));
        RenameProcessor processor;
        // If the qualified class name couldn't be found, try using the class name as file name and find that file
        if(psiClass == null) {
            Utils utils = new Utils(project);
            String filePath = original.getLocationInfo().getFilePath();
            psiClass = utils.getPsiClassByFilePath(filePath, qualifiedClass);
        }
        // Get the methods in the class
        assert psiClass != null;
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        PsiMethod[] methods = psiClass.getMethods();
        // Find the method being refactored
        for (PsiMethod method : methods) {
            // Check that the signatures are the same
            if(Utils.ifSameMethods(method, renamed)) {
                // Create a new rename processor using the original method name and the refactored method that we
                // found
                processor = new RenameProcessor(project, method, srcName, false, false);
                Application app = ApplicationManager.getApplication();
                // Run the rename class processor in the current modality state
                app.invokeAndWait(processor, ModalityState.current());
                // Update the virtual file that contains the refactoring
                vFile.refresh(false, true);
                break;
            }
        }

    }

    /*
     * Undo the class refactoring that was originally performed.
     */
    public void undoRenameClass(Refactoring ref) {

        UMLClass original = ((RenameClassRefactoring) ref).getOriginalClass();
        UMLClass renamed = ((RenameClassRefactoring) ref).getRenamedClass();
        String srcClassName = original.getName();
        String destPackageName = renamed.getPackageName();
        String destClassName = renamed.getName();
        String qualifiedClass = destPackageName + "." + destClassName;
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(project);
        PsiClass psiClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope((project)));
        // If the class isn't found, there might not have been a gradle file and we need to find the class another way
        if(psiClass == null) {
            Utils utils = new Utils(project);
            String filePath = original.getLocationInfo().getFilePath();
            psiClass = utils.getPsiClassByFilePath(filePath, qualifiedClass);
        }
        // Create a rename processor using the original name of the class and the psi class
        assert psiClass != null;
        RenameProcessor processor = new RenameProcessor(project, psiClass, srcClassName, false, false);
        Application app = ApplicationManager.getApplication();
        // Run the rename class processor in the current modality state
        app.invokeAndWait(processor, ModalityState.current());
        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

}
