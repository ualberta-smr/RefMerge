package ca.ualberta.cs.smr.core.refactoringObjects;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

import java.util.List;

public class ExtractOperationRefactoringWrapper extends ExtractOperationRefactoring {
    private final PsiElement[] surroundingElements;
    private final ThrownExceptionInfo[] thrownExceptionInfos;

    public ExtractOperationRefactoringWrapper(UMLOperationBodyMapper bodyMapper,
                                              UMLOperation sourceOperationAfterExtraction,
                                              List<OperationInvocation> operationInvocations,
                                              PsiElement[] surroundingElements,
                                              ThrownExceptionInfo[] thrownExceptionInfos) {
        super(bodyMapper, sourceOperationAfterExtraction, operationInvocations);
        this.surroundingElements = surroundingElements;
        this.thrownExceptionInfos = thrownExceptionInfos;
    }

    public PsiElement[] getSurroundingElements() {
        return surroundingElements;
    }

    public ThrownExceptionInfo[] getThrownExceptionInfos() {
        return thrownExceptionInfos;
    }
}
