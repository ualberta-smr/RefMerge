package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.inline.InlineMethodProcessor;

public class ReplayInlineMethod {

    Project project;

    public ReplayInlineMethod(Project project) {
        this.project = project;
    }

    /*
     * Replay the inline method refactoring by performing an inline method refactoring.
     */
    public void replayInlineMethod(RefactoringObject ref) {
        InlineMethodObject inlineMethodObject = (InlineMethodObject) ref;

        MethodSignatureObject originalOperation = inlineMethodObject.getOriginalMethodSignature();

        // Get PSI Method using originalOperation data
        String originalMethodClassName = inlineMethodObject.getOriginalClassName();
        String filePath = inlineMethodObject.getOriginalFilePath();
        Utils utils = new Utils(project);
        utils.addSourceRoot(filePath, originalMethodClassName);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(originalMethodClassName, filePath);
        if(psiClass == null) {
            return;
        }
        PsiMethod originalMethod = Utils.getPsiMethod(psiClass, originalOperation);
        if(originalMethod == null) {
            return;
        }

        PsiJavaCodeReferenceElement referenceElement = Utils.getPsiReferenceExpressionsForExtractMethod(originalMethod, project);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        InlineMethodProcessor inlineMethodProcessor = new InlineMethodProcessor(project, originalMethod, referenceElement,
                editor, false);
        Application app = ApplicationManager.getApplication();
        app.invokeAndWait(inlineMethodProcessor);

        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
    }
}
