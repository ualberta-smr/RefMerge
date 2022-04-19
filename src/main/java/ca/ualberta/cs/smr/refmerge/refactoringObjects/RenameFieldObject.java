package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ClassObject;
import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.diff.RenameAttributeRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class RenameFieldObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;
    private String originalFilePath;
    private String destinationFilePath;
    private ClassObject originalClassObject;
    private ClassObject destinationClassObject;
    private int startOffset;
    private boolean isReplay;
    private boolean isRenameMethod;
    private boolean isMoveMethod;
    private boolean isSameFile;
    private boolean isMoveInner;
    private boolean isMoveInnerToInner;
    private boolean isMoveOuter;
    private int startLine;
    private int endLine;


    /*
     * Initialize the fields for RenameFieldObject for inverting, replaying, and checking for refactoring conflicts.
     */
    public RenameFieldObject(Refactoring refactoring) {
        RenameAttributeRefactoring ref = (RenameAttributeRefactoring) refactoring;
        this.refactoringType = ref.getRefactoringType();
        this.refactoringDetail = ref.toString();
        this.originalFilePath = ref.getClassNameBefore();
        this.destinationFilePath = ref.getClassNameAfter();


        this.isReplay = false;
    }


    @Override
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    @Override
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public int getEndLine() {
        return endLine;
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
        return RefactoringOrder.RENAME_FIELD;
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
        return destinationFilePath;
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
