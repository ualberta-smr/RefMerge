package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InvertInlineMethod {

    Project project;

    public InvertInlineMethod(Project project) {
        this.project = project;
    }

    /*
     * Invert the inline method refactoring that was originally performed by performing extract method. Use the text offset
     * to move the extracted method to the correct spot within the class.
     */
    public void invertInlineMethod(RefactoringObject ref) {
        InlineMethodObject inlineMethodObject = (InlineMethodObject) ref;
        // Original method that is being inlined
        MethodSignatureObject originalOperation = inlineMethodObject.getOriginalMethodSignature();
        // Method signature for target method of inline method refactoring
        MethodSignatureObject targetOperation = inlineMethodObject.getDestinationMethodSignature();
        // target method's class and file to get PSI method
        String targetOperationClassName = inlineMethodObject.getDestinationClassName();
        String filePath = inlineMethodObject.getDestinationFilePath();
        // Get PSI method of target method
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(targetOperationClassName, filePath);
        if(psiClass == null) {
            return;
        }
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, targetOperation);
        if(psiMethod == null) {
            return;
        }

        String extractedMethodName = originalOperation.getName();
        String targetOperationName = targetOperation.getName();
        String helpId = "Undo inline method";
        PsiType forcedReturnType = getPsiReturnType(originalOperation, psiMethod);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        Set<AbstractCodeFragment> inlinedCodeFragments = inlineMethodObject.getInlinedCodeFragments();
        PsiElement[] psiElements = getElementsFromTargetOperationFragments(psiMethod,inlinedCodeFragments);
        if(psiElements == null) {
            return;
        }
        ExtractMethodProcessor extractMethodProcessor = new ExtractMethodProcessor(project, editor, psiElements,
                forcedReturnType, extractedMethodName, targetOperationName, helpId);
        extractMethodProcessor.setMethodName(extractedMethodName);
        String visibility = originalOperation.getVisibility();
        extractMethodProcessor.setMethodVisibility(visibility);
        try {
            extractMethodProcessor.prepare();
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }
        extractMethodProcessor.setDataFromInputVariables();
        ExtractMethodHandler.extractMethod(project, extractMethodProcessor);

        // The method signature could change if code was added/deleted after inlining it
        PsiMethod extractedMethod = extractMethodProcessor.getExtractedMethod();
        updateMethodSignature(ref, extractedMethod);

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }


    /*
     * Gets the return type of the original method.
     */
    private PsiType getPsiReturnType(MethodSignatureObject methodSignature, PsiMethod psiMethod) {
        ParameterObject returnParameter = methodSignature.getReturnParameter();
        String parameterType = returnParameter.getType();
        PsiElementFactory factory = PsiElementFactory.getInstance(project);
        return factory.createTypeFromText(parameterType, psiMethod);
    }

    /*
     * Get the PSI elements to be extracted from the inlined code fragments.
     */
    private PsiElement[] getElementsFromTargetOperationFragments(PsiMethod psiMethod, Set<AbstractCodeFragment> codeFragments) {
        PsiElement[] firstAndLastElements = new PsiElement[2];
        Iterator<AbstractCodeFragment> iterator = codeFragments.iterator();
        AbstractCodeFragment firstFragment = iterator.next();
        AbstractCodeFragment lastFragment = null;
        for(AbstractCodeFragment codeFragment : codeFragments) {
            lastFragment = codeFragment;
        }
        firstAndLastElements[0] = getPsiElementFromCodeFragment(firstFragment, psiMethod);
        assert lastFragment != null;
        firstAndLastElements[1] = getPsiElementFromCodeFragment(lastFragment, psiMethod);
        if(firstAndLastElements[0] == null || firstAndLastElements[1] == null) {
            return null;
        }
        List<PsiElement> psiElements = PsiTreeUtil.getElementsOfRange(firstAndLastElements[0], firstAndLastElements[1]);
        return psiElements.toArray(new PsiElement[0]);
    }

    /*
     * Use the code fragment to get the PSI element inside of the method
     */
    private PsiElement getPsiElementFromCodeFragment(final AbstractCodeFragment inlinedFragment,
                                                     PsiMethod psiMethod) {
        final PsiElement[] psiElement = new PsiElement[1];
        final String sourceText = Utils.formatText(inlinedFragment.getString());
        psiMethod.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if(psiElement[0] != null) {
                    return;
                }
                String elementsText = Utils.formatText(element.getText());
                if(sourceText.equals(elementsText)) {
                    psiElement[0] = element;
                }
            }
        });
        return psiElement[0];
    }

    /*
     * Update the stored original method signature to match the extracted method.
     */
    private void updateMethodSignature(RefactoringObject refactoringObject, PsiMethod psiMethod) {
        PsiParameterList psiParameterList = psiMethod.getParameterList();
        PsiParameter[] psiParameters = psiParameterList.getParameters();
        List<ParameterObject> parameterObjects = new ArrayList<>();
        ParameterObject returnParameter = ((InlineMethodObject) refactoringObject).getOriginalMethodSignature().getReturnParameter();
        parameterObjects.add(returnParameter);
        for (PsiParameter psiParameter : psiParameters) {
            String parameterType = psiParameter.getText();
            String parameterName = psiParameter.getName();
            ParameterObject parameterObject = new ParameterObject(parameterType, parameterName);
            parameterObjects.add(parameterObject);
        }
        String methodName = psiMethod.getName();
        MethodSignatureObject methodSignature = new MethodSignatureObject(parameterObjects, methodName);
        ((InlineMethodObject) refactoringObject).setOriginalMethodSignature(methodSignature);


    }
}
