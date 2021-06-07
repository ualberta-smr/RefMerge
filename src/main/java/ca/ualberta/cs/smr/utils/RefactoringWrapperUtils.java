package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.core.refactoringWrappers.ExtractOperationRefactoringWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

import java.util.List;

public class RefactoringWrapperUtils {

    public static ExtractOperationRefactoringWrapper wrapExtractOperation(ExtractOperationRefactoring extractOperationRefactoring,
                                                                          PsiElement[] psiElements,
                                                                          ThrownExceptionInfo[] thrownExceptionInfos) {
        UMLOperationBodyMapper bodyMapper = extractOperationRefactoring.getBodyMapper();
        UMLOperation sourceOperationAfterExtraction = extractOperationRefactoring.getSourceOperationAfterExtraction();
        List<OperationInvocation> operationInvocations = extractOperationRefactoring.getExtractedOperationInvocations();
        return new ExtractOperationRefactoringWrapper(bodyMapper, sourceOperationAfterExtraction, operationInvocations,
                psiElements, thrownExceptionInfos);
    }
}
