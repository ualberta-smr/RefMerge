package ca.ualberta.cs.smr.core.refactoringObjects;

import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

/*
 * Represents an inline method refactoring operation. Contains the necessary information for logic checks and performing the
 * refactoring using the IntelliJ refactoring engine.
 */
public class InlineMethodObject implements RefactoringObject {

    private RefactoringType refactoringType;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalClassName;
    private String destinationClassName;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject destinationMethodSignature;
    private List<OperationInvocation> invocations;
    private boolean isReplay;

    public InlineMethodObject(Refactoring refactoring) {
        InlineOperationRefactoring operation = (InlineOperationRefactoring) refactoring;
        UMLOperation originalOperation = operation.getInlinedOperation();
        UMLOperation destinationOperation = operation.getTargetOperationAfterInline();
        this.refactoringType = operation.getRefactoringType();
        this.originalFilePath = originalOperation.getLocationInfo().getFilePath();
        this.destinationFilePath = destinationOperation.getLocationInfo().getFilePath();
        this.originalClassName = originalOperation.getClassName();
        this.destinationClassName = destinationOperation.getClassName();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(destinationOperation.getName(), destinationOperation.getParameters(),
                destinationOperation.isConstructor(), destinationOperation.getVisibility(), destinationOperation.isStatic());
        this.invocations = operation.getInlinedOperationInvocations();
        this.isReplay = true;
    }



    @Override
    public RefactoringType getRefactoringType() {
        return this.refactoringType;
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

    public List<OperationInvocation> getMethodInvocations() {
        return this.invocations;
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
