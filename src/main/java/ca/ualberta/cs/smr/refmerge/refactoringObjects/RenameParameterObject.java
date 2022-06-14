package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.diff.RenameVariableRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class RenameParameterObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject refactoredMethodSignature;
    private String originalClassName;
    private String refactoredClassName;
    private ParameterObject originalParameterObject;
    private ParameterObject refactoredParameterObject;
    private String originalFilePath;
    private String refactoredFilePath;
    private Boolean isReplay;

    public RenameParameterObject(String originalClassName, String refactoredClassName,
                                 MethodSignatureObject originalMethodSignature, MethodSignatureObject destinationMethodSignature,
                                 ParameterObject originalParameterObject, ParameterObject refactoredParameterObject) {
        this.refactoringType = RefactoringType.RENAME_PARAMETER;
        this.refactoringDetail = "";
        this.originalMethodSignature = originalMethodSignature;
        this.refactoredMethodSignature = destinationMethodSignature;
        this.originalClassName = originalClassName;
        this.refactoredClassName = refactoredClassName;
        this.originalFilePath = "";
        this.refactoredFilePath = "";
        this.originalParameterObject = originalParameterObject;
        this.refactoredParameterObject = refactoredParameterObject;
    }

    public RenameParameterObject(Refactoring refactoring) {
        this.refactoringType = refactoring.getRefactoringType();
        this.refactoringDetail = refactoring.toString();
        RenameVariableRefactoring renameParameterRefactoring = (RenameVariableRefactoring) refactoring;
        VariableDeclaration originalParameter = renameParameterRefactoring.getOriginalVariable();
        VariableDeclaration renamedParameter = renameParameterRefactoring.getRenamedVariable();
        String originalParameterName = originalParameter.getVariableName();
        String refactoredParameterName = renamedParameter.getVariableName();
        String originalParameterType = originalParameter.getType().toString();
        String refactoredParameterType = renamedParameter.getType().toString();
        UMLOperation originalOperation = renameParameterRefactoring.getOperationBefore();
        UMLOperation refactoredOperation = renameParameterRefactoring.getOperationAfter();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.refactoredMethodSignature = new MethodSignatureObject(refactoredOperation.getName(), refactoredOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.originalClassName = originalOperation.getClassName();
        this.refactoredClassName = refactoredOperation.getClassName();
        this.originalFilePath = originalOperation.getLocationInfo().getFilePath();
        this.refactoredFilePath = refactoredOperation.getLocationInfo().getFilePath();
        this.originalParameterObject = new ParameterObject(originalParameterType, originalParameterName);
        this.refactoredParameterObject = new ParameterObject(refactoredParameterType, refactoredParameterName);
        this.isReplay = false;
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
        return RefactoringOrder.RENAME_PARAMETER;
    }

    public ParameterObject getOriginalParameterObject() {
        return originalParameterObject;
    }

    public ParameterObject getRefactoredParameterObject() {
        return refactoredParameterObject;
    }

    public void setOriginalParameterObject(String type, String name) {
        this.originalParameterObject = new ParameterObject(type, name);
    }

    public void setRefactoredParameterObject(String type, String name) {
        this.refactoredParameterObject = new ParameterObject(type, name);
    }

    public MethodSignatureObject getOriginalMethodSignature() {
        return originalMethodSignature;
    }

    public void setOriginalMethodSignature(MethodSignatureObject originalMethodSignature) {
        this.originalMethodSignature = originalMethodSignature;
    }

    public MethodSignatureObject getRefactoredMethodSignature() {
        return refactoredMethodSignature;
    }

    public void setRefactoredMethodSignature(MethodSignatureObject refactoredMethodSignature) {
        this.refactoredMethodSignature = refactoredMethodSignature;
    }

    public String getOriginalClassName() {
        return originalClassName;
    }

    public void setOriginalClassName(String originalClassName) {
        this.originalClassName = originalClassName;
    }

    public String getRefactoredClassName() {
        return refactoredClassName;
    }

    public void setRefactoredClassName(String refactoredClassName) {
        this.refactoredClassName = refactoredClassName;
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
        this.refactoredFilePath = destinationFilePath;
    }

    @Override
    public String getDestinationFilePath() {
        return refactoredFilePath;
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
