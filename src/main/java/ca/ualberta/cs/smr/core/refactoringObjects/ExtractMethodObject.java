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

    public ExtractMethodObject() {
        this.refactoringType = RefactoringType.EXTRACT_OPERATION;
    }

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

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public String getOriginalFilePath() {
        return this.originalFilePath;
    }

    public void setDestinationFilePath(String destinationFilePath) {
        this.destinationFilePath = destinationFilePath;
    }

    public String getDestinationFilePath() {
        return this.destinationFilePath;
    }

    public void setOriginalMethodName(String originalMethodName) {
        this.originalMethodName = originalMethodName;
    }

    public String getOriginalMethodName() {
        return this.originalMethodName;
    }

    public void setDestinationMethodName(String destinationMethodName) {
        this.destinationMethodName = destinationMethodName;
    }

    public String getDestinationMethodName() {
        return this.destinationMethodName;
    }

    public void setOriginalClassName(String originalClassName) {
        this.originalClassName = originalClassName;
    }

    public String getOriginalClassName() {
        return this.originalClassName;
    }

    public void setDestinationClassName(String destinationClassName) {
        this.destinationClassName = destinationClassName;
    }

    public String getDestinationClassName() {
        return this.destinationClassName;
    }

    public void getSurroundingElements(PsiElement[] surroundingElements) {
        this.surroundingElements = surroundingElements;
    }

    public PsiElement[] getSurroundingElements() {
        return this.surroundingElements;
    }

    public void setThrownExceptionInfo(ThrownExceptionInfo[] thrownExceptionInfo) {
        this.thrownExceptionInfo = thrownExceptionInfo;
    }

    public ThrownExceptionInfo[] getThrownExceptionInfo() {
        return this.thrownExceptionInfo;
    }

}
