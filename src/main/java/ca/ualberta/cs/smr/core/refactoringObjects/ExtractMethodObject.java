package ca.ualberta.cs.smr.core.refactoringObjects;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class ExtractMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalMethodName;
    private String destinationMethodName;
    private String originalClassName;
    private String destinationClassName;
    private PsiElement[] surroundingElements;
    private ThrownExceptionInfo[] thrownExceptionInfo;

    public ExtractMethodObject(Refactoring refactoring) {
        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        this.originalFilePath = extractOperationRefactoring.getSourceOperationBeforeExtraction().getLocationInfo().getFilePath();
        this.destinationFilePath = extractOperationRefactoring.getExtractedOperation().getLocationInfo().getFilePath();
        this.originalMethodName = extractOperationRefactoring.getSourceOperationBeforeExtraction().getName();
        this.destinationMethodName = extractOperationRefactoring.getExtractedOperation().getName();
        this.originalClassName = extractOperationRefactoring.getSourceOperationBeforeExtraction().getClassName();
        this.destinationClassName = extractOperationRefactoring.getExtractedOperation().getClassName();
        this.surroundingElements = null;
        this.thrownExceptionInfo = null;

    }

    public RefactoringType getRefactoringType() {
        return this.refactoringType;
    }


}
