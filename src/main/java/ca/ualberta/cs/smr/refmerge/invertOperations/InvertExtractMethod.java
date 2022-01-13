package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
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
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;
import com.intellij.util.Query;
import gr.uom.java.xmi.decomposition.OperationInvocation;

public class InvertExtractMethod {

    Project project;

    public InvertExtractMethod(Project project) {
        this.project = project;
    }

    /*
     * Invert the extract method refactoring that was originally performed by performing an inline method refactoring.
     */
    public RefactoringObject invertExtractMethod(RefactoringObject ref) {
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) ref;
        MethodSignatureObject originalMethod = extractMethodObject.getOriginalMethodSignature();
        MethodSignatureObject destinationMethod = extractMethodObject.getDestinationMethodSignature();

        // Get PSI Method using extractedOperation data
        String extractedOperationClassName = extractMethodObject.getOriginalClassName();
        String filePath = extractMethodObject.getOriginalFilePath();
        Utils utils = new Utils(project);
        utils.addSourceRoot(filePath, extractedOperationClassName);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(extractedOperationClassName, filePath);
        if(psiClass == null) {
            return null;
        }
        PsiMethod extractedMethod = Utils.getPsiMethod(psiClass, destinationMethod);
        if(extractedMethod == null) {
            return null;
        }

        ThrownExceptionInfo[] thrownExceptionInfo = getThrownExceptionInfo(extractedMethod);

        String sourceOperationClassName = extractMethodObject.getOriginalClassName();
        filePath = extractMethodObject.getOriginalFilePath();
        psiClass = utils.getPsiClassFromClassAndFileNames(sourceOperationClassName, filePath);
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, originalMethod);
        if(psiMethod == null) {
            return null;
        }
        // Get the first method invocation
        OperationInvocation methodInvocation = extractMethodObject.getMethodInvocations().get(0);

        // Get the statements that surround the method invocation
        PsiElement[] surroundingElements = getSurroundingElements(psiMethod, extractedMethod, methodInvocation);
        if(surroundingElements == null) {
            return null;
        }
        if(surroundingElements[0] == null || surroundingElements[1] == null) {
            return null;
        }
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

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

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
        if(psiReferences.findFirst() == null) {
            return null;
        }
        PsiElement psiElement = psiReferences.findFirst().getElement();

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
        PsiElement prevSibling;
        PsiElement nextSibling;
        if(psiParent == null) {
            psiParent = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
            if(psiParent == null) {
                return null;
            }
            PsiElement candidateElement = psiParent.getParent();
            if(candidateElement instanceof PsiReturnStatement) {
                psiParent = candidateElement;
            }
        }
        if(psiParent.getPrevSibling() == null) {
            prevSibling = psiParent;
        }
        else {
            prevSibling = psiParent.getPrevSibling();
        }
        if(psiParent.getNextSibling() == null) {
            nextSibling = psiParent;
        }
        else {
            nextSibling = psiParent.getNextSibling();
        }
        if(prevSibling instanceof PsiWhiteSpace) {
            prevSibling = prevSibling.getPrevSibling();
        }
        // Handle when previous sibling is the start of the code block
        surroundingElements[0] = prevSibling;
        if(nextSibling instanceof PsiWhiteSpace) {
            nextSibling = nextSibling.getNextSibling();
        }
        // Handle case when next sibling is the end of the code block
        surroundingElements[1] = nextSibling;
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
