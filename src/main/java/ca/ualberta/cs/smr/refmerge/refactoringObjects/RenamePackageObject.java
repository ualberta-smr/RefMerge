package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import gr.uom.java.xmi.diff.RenamePackageRefactoring;
import gr.uom.java.xmi.diff.RenamePattern;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class RenamePackageObject implements  RefactoringObject {

    private RefactoringType refactoringType;
    private String refactoringDetail;
    private String originalName;
    private String destinationName;
    private boolean isReplay;

    public RenamePackageObject(String originalName, String destinationName) {
        this.originalName = originalName;
        this.destinationName = destinationName;
        this.refactoringType = RefactoringType.RENAME_PACKAGE;
        this.refactoringDetail = "";
        this.isReplay = true;
    }

    public RenamePackageObject(Refactoring refactoring) {
        this.refactoringType = refactoring.getRefactoringType();
        this.refactoringDetail = refactoring.toString();
        RenamePackageRefactoring renamePackageRefactoring = (RenamePackageRefactoring) refactoring;
        RenamePattern renamePattern = renamePackageRefactoring.getPattern();
        this.originalName = renamePattern.getBefore();
        this.destinationName = renamePattern.getAfter();
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
        return RefactoringOrder.RENAME_PACKAGE;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    @Override
    public void setOriginalFilePath(String originalFilePath) {

    }

    @Override
    public String getOriginalFilePath() {
        return null;
    }

    @Override
    public void setDestinationFilePath(String destinationFilePath) {

    }

    @Override
    public String getDestinationFilePath() {
        return null;
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
