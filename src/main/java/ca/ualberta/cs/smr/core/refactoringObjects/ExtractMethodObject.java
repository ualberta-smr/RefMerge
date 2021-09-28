package ca.ualberta.cs.smr.core.refactoringObjects;

import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.refactoring.changeSignature.ThrownExceptionInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;
import java.util.Set;

/*
 * Represents an extract method refactoring. Contains the necessary information for logic checks and performing the
 * refactoring using the IntelliJ refactoring engine.
 */
public class ExtractMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalClassName;
    private String destinationClassName;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject destinationMethodSignature;
    private List<OperationInvocation> methodInvocations;
    private Set<AbstractCodeFragment> sourceCodeFragments;
    private Set<AbstractCodeFragment> extractedCodeFragments;
    private CodeRange codeRange;
    private SmartPsiElementPointer[] surroundingElements;
    private ThrownExceptionInfo[] thrownExceptionInfo;
    private boolean isReplay;
    private int startLine;
    private int endLine;

    /*
     * Use the provided information to create the extract method object for testing.
     */
    public ExtractMethodObject(String originalFilePath, String originalClassName, MethodSignatureObject originalMethodSignature,
                              String destinationFilePath, String destinationClassName, MethodSignatureObject destinationMethodSignature) {
        this.refactoringType = RefactoringType.EXTRACT_OPERATION;
        this.refactoringDetail = "";
        this.originalFilePath = originalFilePath;
        this.originalClassName = originalClassName;
        this.originalMethodSignature = originalMethodSignature;
        this.destinationFilePath = destinationFilePath;
        this.destinationClassName = destinationClassName;
        this.destinationMethodSignature = destinationMethodSignature;
        this.isReplay = true;
    }

    /*
     * Creates the extract method object and takes the information that we need from the RefMiner refactoring object.
     */
    public ExtractMethodObject(Refactoring refactoring) {
        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) refactoring;
        UMLOperation originalOperation = extractOperationRefactoring.getSourceOperationBeforeExtraction();
        UMLOperation destinationOperation = extractOperationRefactoring.getExtractedOperation();
        this.refactoringType = refactoring.getRefactoringType();
        this.refactoringDetail = refactoring.toString();
        this.originalFilePath = originalOperation.getLocationInfo().getFilePath();
        this.destinationFilePath = destinationOperation.getLocationInfo().getFilePath();
        this.originalClassName = originalOperation.getClassName();
        this.destinationClassName = destinationOperation.getClassName();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(destinationOperation.getName(), destinationOperation.getParameters(),
                destinationOperation.isConstructor(), destinationOperation.getVisibility(), destinationOperation.isStatic());
        this.methodInvocations = extractOperationRefactoring.getExtractedOperationInvocations();
        this.sourceCodeFragments = extractOperationRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        this.extractedCodeFragments = extractOperationRefactoring.getExtractedCodeFragmentsToExtractedOperation();
        this.codeRange = extractOperationRefactoring.getExtractedCodeRangeFromSourceOperation();
        this.surroundingElements = null;
        this.thrownExceptionInfo = null;
        this.isReplay = true;
        this.startLine = 0;
        this.endLine = 0;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public RefactoringType getRefactoringType() {
        return this.refactoringType;
    }

    public String getRefactoringDetail() {
        return this.refactoringDetail;
    }

    public RefactoringOrder getRefactoringOrder() {
        return RefactoringOrder.EXTRACT_METHOD;
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

    public MethodSignatureObject getOriginalMethodSignature() {
        return originalMethodSignature;
    }

    public void setOriginalMethodSignature(MethodSignatureObject originalMethodSignature) {
        this.originalMethodSignature = originalMethodSignature;
    }

    public MethodSignatureObject getDestinationMethodSignature() {
        return destinationMethodSignature;
    }

    public void setDestinationMethodSignature(MethodSignatureObject destinationMethodSignature) {
        this.destinationMethodSignature = destinationMethodSignature;
    }

    public List<OperationInvocation> getMethodInvocations() {
        return this.methodInvocations;
    }

    public Set<AbstractCodeFragment> getExtractedCodeFragments() {
        return this.extractedCodeFragments;
    }

    public Set<AbstractCodeFragment> getSourceCodeFragments() {
        return this.sourceCodeFragments;
    }

    public CodeRange getCodeRange() {
        return this.codeRange;
    }

    public void setSurroundingElements(SmartPsiElementPointer[] surroundingElements) {
        this.surroundingElements = surroundingElements;
    }

    public SmartPsiElementPointer[] getSurroundingElements() {
        return this.surroundingElements;
    }

    public void setThrownExceptionInfo(ThrownExceptionInfo[] thrownExceptionInfo) {
        this.thrownExceptionInfo = thrownExceptionInfo;
    }

    public ThrownExceptionInfo[] getThrownExceptionInfo() {
        return this.thrownExceptionInfo;
    }

    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    public boolean isReplay() {
        return isReplay;
    }

}
