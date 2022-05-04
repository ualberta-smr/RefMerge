package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.RenameAttributeRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class MoveRenameFieldObject implements RefactoringObject {

    private RefactoringType refactoringType;
    private final String refactoringDetail;
    private String originalFilePath;
    private String destinationFilePath;
    private String originalName;
    private String destinationName;
    private boolean isReplay;

    private String originalClass;

    private String destinationClass;

    private String visibility;

    private int startLine;
    private int endLine;
    private boolean isRename;

    private boolean isMove;

    /*
     * Initialize for testing
     */
    public MoveRenameFieldObject(String originalFile, String originalClass, String originalname,
                                 String newFile, String newClass, String newName) {
        this.originalFilePath = originalFile;
        this.originalClass = originalClass;
        this.originalName = originalname;
        this.destinationFilePath = newFile;
        this.destinationClass = newClass;
        this.destinationName = newName;
        this.refactoringDetail = "";
    }

    /*
     * Initialize the fields for RenameFieldObject for inverting, replaying, and checking for refactoring conflicts.
     */
    public MoveRenameFieldObject(Refactoring refactoring) {
        this.isMove = false;
        this.isRename = false;
        if(refactoring instanceof RenameAttributeRefactoring) {
            RenameAttributeRefactoring ref = (RenameAttributeRefactoring) refactoring;
            this.refactoringType = ref.getRefactoringType();
            this.refactoringDetail = ref.toString();
            this.originalFilePath = ref.getClassNameBefore();
            this.destinationFilePath = ref.getClassNameAfter();
            UMLAttribute originalAttribute = ref.getOriginalAttribute();
            UMLAttribute destinationAttribute = ref.getRenamedAttribute();
            this.originalName = originalAttribute.getName();
            this.destinationName = destinationAttribute.getName();
            this.originalClass = originalAttribute.getClassName();
            this.destinationClass = destinationAttribute.getClassName();
            setType(refactoringType);
        }
        // MoveAndRenameAttributeRefactoring is a subclass of MoveAttributeRefactoring so this should cover both cases
        else  {
            MoveAttributeRefactoring ref = (MoveAttributeRefactoring) refactoring;
            this.refactoringType = ref.getRefactoringType();
            this.refactoringDetail = ref.toString();
            this.originalClass = ref.getSourceClassName();
            this.destinationClass = ref.getTargetClassName();
            UMLAttribute originalAttribute = ref.getOriginalAttribute();
            UMLAttribute destinationAttribute = ref.getMovedAttribute();
            this.originalName = originalAttribute.getName();
            this.destinationName = destinationAttribute.getName();
            this.originalFilePath = originalAttribute.getLocationInfo().getFilePath();
            this.destinationFilePath = destinationAttribute.getLocationInfo().getFilePath();
            setType(refactoringType);
            this.visibility = originalAttribute.getVisibility();
        }


        this.isReplay = true;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getOriginalClass() {
        return originalClass;
    }

    public String getDestinationClass() { return destinationClass; }

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
        return RefactoringOrder.MOVE_RENAME_FIELD;
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

    public boolean isRename() {
        return isRename;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isMove() {
        return isMove;
    }

    public void setOriginalClassName(String originalClass) {
        this.originalClass = originalClass;
    }

    public void setDestinationClassName(String destinationClass) {
        this.destinationClass = destinationClass;
    }

    public void setDestinationFieldName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setType(RefactoringType refactoringType) {
        if(refactoringType.equals(RefactoringType.RENAME_ATTRIBUTE)) {
            this.isRename = true;
        }
        if(refactoringType.equals(RefactoringType.MOVE_ATTRIBUTE)) {
            this.isMove = true;
        }
        else {
            this.isRename = true;
            this.isMove = true;
        }
        if(isRename && isMove) {
            this.refactoringType = RefactoringType.MOVE_RENAME_ATTRIBUTE;
        }
    }
}
