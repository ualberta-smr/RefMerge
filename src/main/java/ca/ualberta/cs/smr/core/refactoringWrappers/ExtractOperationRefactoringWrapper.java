package ca.ualberta.cs.smr.core.refactoringWrappers;

import com.intellij.psi.PsiStatement;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;

import java.util.List;

public class ExtractOperationRefactoringWrapper extends ExtractOperationRefactoring {
    private final PsiStatement[] surroundingStatements;
    private final ThrownExceptionInfo[] thrownExceptionInfos;

    public ExtractOperationRefactoringWrapper(UMLOperationBodyMapper bodyMapper,
                                              UMLOperation sourceOperationAfterExtraction,
                                              List<OperationInvocation> operationInvocations,
                                              PsiStatement[] surroundingStatements,
                                              ThrownExceptionInfo[] thrownExceptionInfos) {
        super(bodyMapper, sourceOperationAfterExtraction, operationInvocations);
        this.surroundingStatements = surroundingStatements;
        this.thrownExceptionInfos = thrownExceptionInfos;
    }

    public PsiStatement[] getSurroundingStatements() {
        return surroundingStatements;
    }

    public ThrownExceptionInfo[] getThrownExceptionInfos() {
        return thrownExceptionInfos;
    }
}
