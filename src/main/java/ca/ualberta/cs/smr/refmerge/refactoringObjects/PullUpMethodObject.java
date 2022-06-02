package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.PullUpOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class PullUpMethodObject implements RefactoringObject {
    private final RefactoringType refactoringType;
    private String originalClass;
    private String newClass;
    private String originalMethodName;
    private String refactoredMethodName;
    private String originalFileName;
    private String refactoredFileName;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject destinationMethodSignature;
    private String refactoringDetail;
    private boolean isReplay;

    public PullUpMethodObject(Refactoring refactoring) {
        PullUpOperationRefactoring pullUpOperation = (PullUpOperationRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        UMLOperation originalOperation =  pullUpOperation.getOriginalOperation();
        UMLOperation refactoredOperation =  pullUpOperation.getMovedOperation();
        this.originalClass = originalOperation.getClassName();
        this.newClass = refactoredOperation.getClassName();
        this.originalMethodName = originalOperation.getName();
        this.refactoredMethodName = refactoredOperation.getName();
        this.originalFileName = originalOperation.getLocationInfo().getFilePath();
        this.refactoredFileName = refactoredOperation.getLocationInfo().getFilePath();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(refactoredOperation.getName(), refactoredOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.refactoringDetail = refactoring.toString();
        this.isReplay = true;

    }


    @Override
    public void setStartLine(int startLine) {

    }

    @Override
    public void setEndLine(int endLine) {

    }

    @Override
    public int getStartLine() {
        return 0;
    }

    @Override
    public int getEndLine() {
        return 0;
    }

    @Override
    public String getRefactoringDetail() {
        return refactoringDetail;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return refactoringType;
    }

    @Override
    public RefactoringOrder getRefactoringOrder() {
        return RefactoringOrder.PULL_UP_METHOD;
    }

    @Override
    public void setOriginalFilePath(String originalFilePath) {
        this.originalFileName = originalFilePath;
    }

    @Override
    public String getOriginalFilePath() {
        return originalFileName;
    }

    @Override
    public void setDestinationFilePath(String destinationFilePath) {
        this.refactoredFileName = destinationFilePath;
    }

    @Override
    public String getDestinationFilePath() {
        return refactoredFileName;
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

    @Override
    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    @Override
    public boolean isReplay() {
        return isReplay;
    }
}
