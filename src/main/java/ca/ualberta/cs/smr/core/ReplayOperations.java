package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import gr.uom.java.xmi.UMLOperation;
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
        RenameProcessor processor;
        // If the PSI class is null, then this part of the project wasn't built and we need to find the PSI class
        // another way
        if(psiClass == null) {
            Utils utils = new Utils(project);
            String filePath = renamed.getLocationInfo().getFilePath();
            psiClass = utils.getPsiClassByFilePath(filePath, qualifiedClass);
        }
        assert psiClass != null;
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
            // If we find the method that needs to be refactored
            if(Utils.ifSameMethods(method, original)) {
                // Create a rename processor using the method and the name that we're refactoring it to
                processor = new RenameProcessor(project, method, destName, false, false);
                // Run the refactoring processor
                ApplicationManager.getApplication().invokeAndWait(processor::doRun, ModalityState.current());
                // Update the virtual file containing the refactoring
                VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
                vFile.refresh(false, true);
                break;
            }
        }
    }

}
