package ca.ualberta.cs.smr.core.refactoringWrappers;

import com.intellij.psi.PsiStatement;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

import java.util.List;

public class RefactoringWrapperUtils {

    public static ExtractOperationRefactoringWrapper wrapExtractOperation(ExtractOperationRefactoring extractOperationRefactoring,
                                                                          PsiStatement[] psiStatements) {
        UMLOperationBodyMapper bodyMapper = extractOperationRefactoring.getBodyMapper();
        UMLOperation sourceOperationAfterExtraction = extractOperationRefactoring.getSourceOperationAfterExtraction();
        List<OperationInvocation> operationInvocations = extractOperationRefactoring.getExtractedOperationInvocations();
        return new ExtractOperationRefactoringWrapper(bodyMapper, sourceOperationAfterExtraction, operationInvocations, psiStatements);
    }
}
