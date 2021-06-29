package ca.ualberta.cs.smr.core.undoOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.changeSignature.JavaThrownExceptionInfo;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import com.intellij.refactoring.inline.InlineMethodProcessor;
import com.intellij.util.Query;
import gr.uom.java.xmi.decomposition.OperationInvocation;

public class UndoExtractMethod {

    Project project;

    public UndoExtractMethod(Project project) {
        this.project = project;
    }

    /*
     * Undo the extract method refactoring that was originally performed by performing an inline method refactoring.
     */
    public RefactoringObject undoExtractMethod(RefactoringObject ref) {
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) ref;
        MethodSignatureObject originalMethod = extractMethodObject.getOriginalMethodSignature();
        MethodSignatureObject destinationMethod = extractMethodObject.getDestinationMethodSignature();

        // Get PSI Method using extractedOperation data
        String extractedOperationClassName = extractMethodObject.getOriginalClassName();
        String filePath = extractMethodObject.getOriginalFilePath();
        Utils utils = new Utils(project);
        utils.addSourceRoot(filePath);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(extractedOperationClassName, filePath);
        assert psiClass != null;
        PsiMethod extractedMethod = Utils.getPsiMethod(psiClass, destinationMethod);
        assert extractedMethod != null;

        ThrownExceptionInfo[] thrownExceptionInfo = getThrownExceptionInfo(extractedMethod);

        String sourceOperationClassName = extractMethodObject.getOriginalClassName();
        filePath = extractMethodObject.getOriginalFilePath();
        psiClass = utils.getPsiClassFromClassAndFileNames(sourceOperationClassName, filePath);
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, originalMethod);
        assert psiMethod != null;
        // Get the first method invocation
        OperationInvocation methodInvocation = extractMethodObject.getMethodInvocations().get(0);

        // Get the statements that surround the method invocation
        PsiElement[] surroundingElements = getSurroundingElements(psiMethod, extractedMethod, methodInvocation);
        extractMethodObject.setThrownExceptionInfo(thrownExceptionInfo);
        SmartPsiElementPointer[] surroundingPointers = new SmartPsiElementPointer[2];
        surroundingPointers[0] = SmartPointerManager.createPointer(surroundingElements[0]);
        surroundingPointers[1] = SmartPointerManager.createPointer(surroundingElements[1]);
        extractMethodObject.setSurroundingElements(surroundingPointers);


        PsiJavaCodeReferenceElement referenceElement = Utils.getPsiReferenceExpressionsForExtractMethod(extractedMethod, project);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        InlineMethodProcessor inlineMethodProcessor = new InlineMethodProcessor(project, extractedMethod, referenceElement,
                editor, false);

        Application app = ApplicationManager.getApplication();
        app.invokeAndWait(inlineMethodProcessor);

        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
        return extractMethodObject;
    }

    /*
     * Get the method invocation and the PSI Elements before and after the method invocation so we can extract the
     * correct code when we replay.
     */
    private PsiElement[] getSurroundingElements(PsiMethod psiMethod, PsiMethod extractedMethod, OperationInvocation methodInvocation) {
        PsiElement[] surroundingElements = new PsiElement[4];
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;

        // Get the correct PSI reference
        Query<PsiReference> psiReferences = ReferencesSearch.search(extractedMethod);
        PsiElement psiElement = (PsiElement) psiReferences.findFirst();
        if(psiElement instanceof PsiMethod) {
            for (PsiReference psiReference : psiReferences) {
                psiElement = (PsiElement) psiReference;
                if (psiElement instanceof PsiMethod) {
                    continue;
                }
                PsiElement containingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
                assert containingMethod != null;
                if (containingMethod.isEquivalentTo(psiMethod)) {
                    break;
                }
            }
        }
        PsiElement psiParent = PsiTreeUtil.getParentOfType(psiElement, PsiDeclarationStatement.class, PsiExpressionStatement.class);
        assert psiParent != null;
        PsiElement prevSibling = psiParent.getPrevSibling();
        if(prevSibling instanceof PsiWhiteSpace) {
            prevSibling = prevSibling.getPrevSibling();
        }
        // Handle when previous sibling is the start of the code block
        if(prevSibling instanceof PsiJavaToken) {
            surroundingElements[0] = prevSibling;
        }
        else {
            surroundingElements[0] = prevSibling;
        }
        PsiElement nextSibling = psiParent.getNextSibling();
        if(nextSibling instanceof PsiWhiteSpace) {
            nextSibling = nextSibling.getNextSibling();
        }
        // Handle case when next sibling is the end of the code block
        if(nextSibling instanceof PsiJavaToken) {
            surroundingElements[1] = nextSibling;
        }
        else {
            surroundingElements[1] = nextSibling;
        }
        return surroundingElements;

    }



    private ThrownExceptionInfo[] getThrownExceptionInfo(PsiMethod psiMethod) {
        int size = psiMethod.getThrowsTypes().length;
        ThrownExceptionInfo[] thrownExceptionInfos = new ThrownExceptionInfo[size];
        for(int i = 0; i < size; i++) {
            JavaThrownExceptionInfo thrownExceptionInfo = new JavaThrownExceptionInfo(i);
            thrownExceptionInfo.updateFromMethod(psiMethod);
            thrownExceptionInfos[i] = thrownExceptionInfo;
        }
        return thrownExceptionInfos;
    }
}
