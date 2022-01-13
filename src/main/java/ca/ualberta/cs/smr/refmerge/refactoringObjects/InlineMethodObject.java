package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;
import java.util.Set;

/*
 * Represents an inline method refactoring operation. Contains the necessary information for logic checks and performing the
 * refactoring using the IntelliJ refactoring engine.
 */
public class InlineMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalClassName;
    private String destinationClassName;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject destinationMethodSignature;
    private List<OperationInvocation> invocations;
    private Set<AbstractCodeFragment> inlinedCodeFragments;
    private int startOffset;
    private boolean isReplay;
    private int startLine;
    private int endLine;


    public InlineMethodObject(String originalFilePath, String originalClassName, MethodSignatureObject originalMethodSignature,
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

    public InlineMethodObject(Refactoring refactoring) {
        InlineOperationRefactoring operation = (InlineOperationRefactoring) refactoring;
        UMLOperation originalOperation = operation.getInlinedOperation();
        UMLOperation destinationOperation = operation.getTargetOperationAfterInline();
        this.refactoringType = operation.getRefactoringType();
        this.refactoringDetail = refactoring.toString();
        this.originalFilePath = originalOperation.getLocationInfo().getFilePath();
        this.destinationFilePath = destinationOperation.getLocationInfo().getFilePath();
        this.originalClassName = originalOperation.getClassName();
        this.destinationClassName = destinationOperation.getClassName();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(destinationOperation.getName(), destinationOperation.getParameters(),
                destinationOperation.isConstructor(), destinationOperation.getVisibility(), destinationOperation.isStatic());
        this.invocations = operation.getInlinedOperationInvocations();
        this.inlinedCodeFragments = operation.getInlinedCodeFragments();
        this.startOffset = originalOperation.getLocationInfo().getStartOffset();
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


    @Override
    public RefactoringType getRefactoringType() {
        return this.refactoringType;
    }

    @Override
    public String getRefactoringDetail() {
        return this.refactoringDetail;
    }

    @Override
    public RefactoringOrder getRefactoringOrder() {
        return RefactoringOrder.INLINE_METHOD;
    }

    @Override
    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    @Override
    public String getOriginalFilePath() {
        return originalFilePath;
    }

    @Override
    public void setDestinationFilePath(String destinationFilePath) {
        this.destinationFilePath = destinationFilePath;
    }

    @Override
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

    public Set<AbstractCodeFragment> getInlinedCodeFragments() {
        return this.inlinedCodeFragments;
    }

    public List<OperationInvocation> getMethodInvocations() {
        return this.invocations;
    }

    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    @Override
    public boolean isReplay() {
        return isReplay;
    }
}
