package ca.ualberta.cs.smr.core.refactoringObjects;

import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class RenameMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalMethodName;
    private String destinationMethodName;
    private String originalClassName;
    private String destinationClassName;

    public RenameMethodObject() {
        this.refactoringType = RefactoringType.RENAME_METHOD;
    }

    public RenameMethodObject(Refactoring refactoring) {
        RenameOperationRefactoring renameOperationRefactoring = (RenameOperationRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        this.originalFilePath = renameOperationRefactoring.getOriginalOperation().getLocationInfo().getFilePath();
        this.destinationFilePath = renameOperationRefactoring.getRenamedOperation().getLocationInfo().getFilePath();
        this.originalMethodName = renameOperationRefactoring.getOriginalOperation().getName();
        this.destinationMethodName = renameOperationRefactoring.getRenamedOperation().getName();
        this.originalClassName = renameOperationRefactoring.getOriginalOperation().getClassName();
        this.destinationClassName = renameOperationRefactoring.getRenamedOperation().getClassName();
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


}
