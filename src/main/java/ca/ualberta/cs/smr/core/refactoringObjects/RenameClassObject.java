package ca.ualberta.cs.smr.core.refactoringObjects;

import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class RenameClassObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalClassName;
    private String destinationClassName;

    public RenameClassObject() {
        this.refactoringType = RefactoringType.RENAME_CLASS;
    }

    public RenameClassObject(Refactoring refactoring) {
        RenameClassRefactoring renameOperationRefactoring = (RenameClassRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        this.originalFilePath = renameOperationRefactoring.getOriginalClass().getLocationInfo().getFilePath();
        this.destinationFilePath = renameOperationRefactoring.getRenamedClass().getLocationInfo().getFilePath();
        this.originalClassName = renameOperationRefactoring.getOriginalClassName();
        this.destinationClassName = renameOperationRefactoring.getRenamedClassName();
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
