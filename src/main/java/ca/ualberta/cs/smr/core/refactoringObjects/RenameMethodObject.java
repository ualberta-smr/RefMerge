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



}
