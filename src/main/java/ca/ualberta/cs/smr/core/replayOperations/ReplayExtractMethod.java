package ca.ualberta.cs.smr.core.replayOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.refactoring.changeSignature.ChangeSignatureProcessor;
import com.intellij.refactoring.changeSignature.ParameterInfoImpl;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.intellij.refactoring.util.duplicates.Match;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.diff.CodeRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ReplayExtractMethod {

    Project project;

    public ReplayExtractMethod(Project project) {
        this.project = project;
    }

    public void replayExtractMethod(RefactoringObject ref) throws Exception {
        ExtractMethodObject extractMethodObject = (ExtractMethodObject) ref;
        MethodSignatureObject sourceOperation = extractMethodObject.getOriginalMethodSignature();
        MethodSignatureObject extractedOperation = extractMethodObject.getDestinationMethodSignature();
        String refactoringName = extractedOperation.getName();
        String initialMethodName = sourceOperation.getName();
        String originalClassName = extractMethodObject.getOriginalClassName();
        String filePath = extractMethodObject.getOriginalFilePath();
        String helpId = "";

        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(originalClassName, filePath);
        if(psiClass == null) {
            return;
        }
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, sourceOperation);
        if(psiMethod == null) {
            return;
        }

        SmartPsiElementPointer[] surroundingElements = extractMethodObject.getSurroundingElements();
        if(surroundingElements == null) {
            return;
        }
        if(surroundingElements[0] == null || surroundingElements[1] == null) {
            return;
        }
        Set<AbstractCodeFragment> sourceFragments = extractMethodObject.getSourceCodeFragments();
        Set<AbstractCodeFragment> extractedFragments = extractMethodObject.getExtractedCodeFragments();
        CodeRange codeRange = extractMethodObject.getCodeRange();
        PsiElement[] psiElements =
                getPsiElementsBetweenElements(surroundingElements, sourceFragments, extractedFragments, codeRange, psiMethod);
        if(psiElements == null) {
            return;
        }

        PsiType forcedReturnType = getPsiReturnType(extractedOperation, psiMethod);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        ExtractMethodProcessor extractMethodProcessor = new ExtractMethodProcessor(project, editor, psiElements,
                forcedReturnType, refactoringName, initialMethodName, helpId);
        extractMethodProcessor.setMethodName(refactoringName);
        String visibility = extractedOperation.getVisibility();
        extractMethodProcessor.setMethodVisibility(visibility);
        try {
            extractMethodProcessor.prepare();
        } catch (PrepareFailedException e) {
            e.printStackTrace();
            return;
        }
        extractMethodProcessor.setDataFromInputVariables();
        ExtractMethodHandler.extractMethod(project, extractMethodProcessor);
        // Check for duplicates and prepare method signature
        if(extractMethodProcessor.initParametrizedDuplicates(false)) {
            // Handle duplicate extract method calls
            handleDuplicates(extractMethodProcessor, extractedOperation, sourceOperation.getName());
        }

        updateSignature(extractMethodProcessor, extractedOperation, refactoringName,
                forcedReturnType, extractMethodObject.getThrownExceptionInfo());
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
    }

    /*
     * Extracts the duplicate extract method calls. After all duplicates have been extracted, it renames the
     * parameters to match what the developers expected after the merge.
     */
    private void handleDuplicates(ExtractMethodProcessor processor, MethodSignatureObject operation, String sourceMethodName) {

        final List<Match> duplicates = processor.getDuplicates();
        for (final Match match : duplicates) {
            if (!match.getMatchStart().isValid() || !match.getMatchEnd().isValid()) continue;
            PsiElement psiElement = match.getMatchStart();
            PsiMethod containingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
            assert containingMethod != null;
            if(!sourceMethodName.equals(containingMethod.getName())) {
                continue;
            }
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            Runnable runnable = () -> ApplicationManager.getApplication().runWriteAction(() -> {
                processor.processMatch(match);
            });
            CommandProcessor.getInstance().executeCommand(project, runnable, "Extract Method", null);
        }
        PsiMethod extractedPsiMethod = processor.getExtractedMethod();
        renameParameters(extractedPsiMethod, operation);
    }

    /*
     * Update the extracted method signature to include the throws exception list and rearrange the parameters to be
     * in the correct order.
     */
    private void updateSignature(ExtractMethodProcessor processor, MethodSignatureObject operation, String refactoringName,
                                 PsiType forcedReturnType, ThrownExceptionInfo[] thrownExceptionInfo) {
        PsiMethod extractedPsiMethod = processor.getExtractedMethod();
        if(extractedPsiMethod.getParameterList().getParametersCount() > 1) {
            ParameterInfoImpl[] parameterInfo = getParameterInfo(extractedPsiMethod, operation);
            // Temporary workaround until we rename the parameters in duplicates
            if(parameterInfo[0] != null) {
                if(thrownExceptionInfo == null) {
                    ChangeSignatureProcessor changeSignatureProcessor =
                            new ChangeSignatureProcessor(project, extractedPsiMethod, false, null,
                                    refactoringName, forcedReturnType, parameterInfo);
                    changeSignatureProcessor.run();
                }
                else {
                    ChangeSignatureProcessor changeSignatureProcessor =
                            new ChangeSignatureProcessor(project, extractedPsiMethod, false, null,
                                    refactoringName, forcedReturnType, parameterInfo, thrownExceptionInfo);
                    changeSignatureProcessor.run();
                }
            }
        }
    }

    /*
     * Use the psi elements that are not replaced by inlining the method at the beginning and end of the extracted method
     *  to get all involved psi statements in the refactoring. If the psi element is null, then use the first and last
     *  extracted fragments to find the psi elements.
     */
    private PsiElement[] getPsiElementsBetweenElements(SmartPsiElementPointer[] surroundingElements,
                                                       Set<AbstractCodeFragment> sourceFragments,
                                                       Set<AbstractCodeFragment> extractedFragments,
                                                       CodeRange codeRange, PsiMethod psiMethod) throws Exception {
        PsiElement firstElement = surroundingElements[0].getElement();
        PsiElement lastElement = surroundingElements[1].getElement();
        // If the first element was invalidated
        if(firstElement == null) {
            AbstractCodeFragment extractedFragment = extractedFragments.iterator().next();
            AbstractCodeFragment sourceFragment = sourceFragments.iterator().next();
            firstElement = getPsiElementFromCodeFragment(sourceFragment, extractedFragment, psiMethod);
        }
        // Otherwise, use the first PSI element to get the first PSI element of the inlined method
        else {
            firstElement = firstElement.getNextSibling();
            if (firstElement instanceof PsiWhiteSpace) {
                firstElement = firstElement.getNextSibling();
            }
        }
        // If the last element wsa invalidated
        if(lastElement == null) {
            AbstractCodeFragment extractedFragment = null;
            for (AbstractCodeFragment extractedCodeFragment : extractedFragments) {
                extractedFragment = extractedCodeFragment;
            }
            AbstractCodeFragment sourceFragment = null;
            for (AbstractCodeFragment sourceCodeFragment : sourceFragments) {
                sourceFragment = sourceCodeFragment;
            }

            assert sourceFragment != null;
            assert extractedFragment != null;
            lastElement = getPsiElementFromCodeFragment(sourceFragment,  extractedFragment, psiMethod);
        }
        else {
            lastElement = lastElement.getPrevSibling();
            if (lastElement instanceof PsiWhiteSpace) {
                lastElement = lastElement.getPrevSibling();
            }
        }

        // If an element is still null and the range is 1, then use the same element for both.
        if(firstElement == null) {
            int range = codeRange.getEndLine() - codeRange.getStartLine();
            if(range == 0) {
                firstElement = lastElement;
            }
        }
        else if(lastElement == null) {
            int range = codeRange.getEndLine() - codeRange.getStartLine();
            if(range == 0) {
                lastElement = firstElement;
            }
        }

        if(firstElement == null) {
            return null;
        }
        if(lastElement == null) {
            return null;
        }
        List<PsiElement> psiElements;
        try {
            psiElements = PsiTreeUtil.getElementsOfRange(firstElement, lastElement);
        }
        catch(IllegalArgumentException e) {
            return null;
        }
        return psiElements.toArray(new PsiElement[0]);
    }

    private PsiElement getPsiElementFromCodeFragment(final AbstractCodeFragment sourceFragment,
                                                     final AbstractCodeFragment extractedFragment,
                                                     PsiMethod psiMethod) {
        final PsiElement[] psiElement = new PsiElement[1];
        final String sourceText = Utils.formatText(sourceFragment.getString());
        final String extractedText = Utils.formatText(extractedFragment.getString());
        psiMethod.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                String elementsText = Utils.formatText(element.getText());
                if(sourceText.equals(elementsText) || extractedText.equals(elementsText)) {
                    psiElement[0] = element;
                }
            }
        });
        return psiElement[0];
    }

    /*
     * Gets the return type of the extracted method.
     */
    private PsiType getPsiReturnType(MethodSignatureObject methodSignature, PsiMethod psiMethod) {
        ParameterObject returnParameter = methodSignature.getReturnParameter();
        String parameterType = returnParameter.getType();
        PsiElementFactory factory = PsiElementFactory.getInstance(project);
        return factory.createTypeFromText(parameterType, psiMethod);
    }

    /*
     * Use the PSI method and UML operation to reorder the parameters in the extracted method.
     */
    private ParameterInfoImpl[] getParameterInfo(PsiMethod psiMethod, MethodSignatureObject methodSignatureObject) {

        List<ParameterObject> parameterList = methodSignatureObject.getParameterList();
        PsiParameterList psiParameterList = psiMethod.getParameterList();
        ParameterInfoImpl[] parameterInfoImplArray = new ParameterInfoImpl[parameterList.size() - 1];
        for(int i = 1; i < parameterList.size(); i++) {
            ParameterObject parameter = parameterList.get(i);
            String umlParameterType = parameter.getType();
            String umlParameterName = parameter.getName();
            ParameterInfoImpl parameterInfo;
            for(PsiParameter psiParameter : psiParameterList.getParameters()) {
                String psiParameterName = psiParameter.getName();
                PsiType psiType = psiParameter.getType();
                String psiParameterType = psiType.getPresentableText();
                if(umlParameterName.equals(psiParameterName) && umlParameterType.equals(psiParameterType)) {
                    int index = psiParameterList.getParameterIndex(psiParameter);
                    parameterInfo = ParameterInfoImpl.create(index).withName(psiParameterName).withType(psiType);
                    parameterInfoImplArray[i-1] = parameterInfo;
                    break;
                }
            }

        }

        return parameterInfoImplArray;
    }

    /*
     * Renames the parameters in the extracted method using the UML parameters detected by RefMiner.
     */
    private void renameParameters(PsiMethod psiMethod, MethodSignatureObject methodSignatureObject) {

        List<ParameterObject> parameterList = methodSignatureObject.getParameterList();
        PsiParameterList psiParameterList = psiMethod.getParameterList();
        ParameterInfoImpl[] parameterInfoImplArray = new ParameterInfoImpl[parameterList.size() - 1];
        // Start at 1 to ignore return type
        for(int i = 1; i < parameterList.size(); i++) {
            ParameterObject parameterObject = parameterList.get(i);
            String parameterObjectName = parameterObject.getName();
            PsiParameter psiParameter = psiParameterList.getParameter(i-1);
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            if(psiParameter == null) {
                return;
            }
            RenameRefactoring renameRefactoring = factory.createRename(psiParameter, parameterObjectName, true, false);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
    }
}
