package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;


/*
 * Represents a rename method refactoring. Contains the necessary information for logic checks and performing the
 * refactoring using the IntelliJ refactoring engine.
 */
public class MoveRenameMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;
    private String originalFilePath;
    private String destinationFilePath;
    private MethodSignatureObject originalMethodSignature;
    private String originalClassName;
    private String destinationClassName;
    private String originalDestinationClassName;
    private MethodSignatureObject destinationMethodSignature;
    private int startOffset;
    private String methodAbove;
    private boolean isRenameMethod;
    private boolean isMoveMethod;
    private boolean isReplay;
    private int startLine;
    private int endLine;

    /*
     * Use the provided information to create the rename method object for testing.
     */
    public MoveRenameMethodObject(String originalFilePath, String originalClassName, MethodSignatureObject originalMethodSignature,
                                  String destinationFilePath, String destinationClassName, MethodSignatureObject destinationMethodSignature) {
        this.refactoringType = RefactoringType.RENAME_METHOD;
        this.refactoringDetail = "";
        this.originalFilePath = originalFilePath;
        this.originalClassName = originalClassName;
        this.originalMethodSignature = originalMethodSignature;
        this.destinationFilePath = destinationFilePath;
        this.destinationClassName = destinationClassName;
        this.destinationMethodSignature = destinationMethodSignature;
        this.originalDestinationClassName = destinationClassName;
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        this.isReplay = true;
        this.startLine = 0;
        this.endLine = 0;

    }

    /*
     * Creates the rename method object and takes the information that we need from the RefMiner refactoring object.
     */
    public MoveRenameMethodObject(Refactoring refactoring) {
        UMLOperation originalOperation;
        UMLOperation destinationOperation;
        if(refactoring instanceof RenameOperationRefactoring) {
            RenameOperationRefactoring renameOperationRefactoring = (RenameOperationRefactoring) refactoring;
            originalOperation = renameOperationRefactoring.getOriginalOperation();
            destinationOperation = renameOperationRefactoring.getRenamedOperation();
        }
        // Both move and rename+move cases
        else {
            MoveOperationRefactoring moveOperationRefactoring = (MoveOperationRefactoring) refactoring;
            originalOperation = moveOperationRefactoring.getOriginalOperation();
            destinationOperation = moveOperationRefactoring.getMovedOperation();
            this.startOffset = originalOperation.getLocationInfo().getStartOffset();
        }
        this.originalFilePath = originalOperation.getLocationInfo().getFilePath();
        this.destinationFilePath = destinationOperation.getLocationInfo().getFilePath();
        this.originalClassName = originalOperation.getClassName();
        this.destinationClassName = destinationOperation.getClassName();
        this.originalDestinationClassName = destinationClassName;
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(destinationOperation.getName(), destinationOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        this.refactoringType = refactoring.getRefactoringType();
        this.refactoringDetail = refactoring.toString();
        setType(refactoringType);
        this.isReplay = true;

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

    public String getRefactoringDetail() {
        return this.refactoringDetail;
    }

    public RefactoringType getRefactoringType() {
        return this.refactoringType;
    }

    public RefactoringOrder getRefactoringOrder() {
        return RefactoringOrder.MOVE_RENAME_METHOD;
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

    public void setOriginalDestinationClassName(String originalDestinationClassName) {
        this.originalDestinationClassName = originalDestinationClassName;
    }

    public String getOriginalDestinationClassName() {
        return this.originalDestinationClassName;
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

    public int getStartOffset() {
        return this.startOffset;
    }

    public void setType(RefactoringType refactoringType) {
        if(refactoringType.equals(RefactoringType.RENAME_METHOD)) {
            this.isRenameMethod = true;
        }
        else if(refactoringType.equals(RefactoringType.MOVE_OPERATION)) {
            this.isMoveMethod = true;
        }
        else {
            this.isRenameMethod = true;
            this.isMoveMethod = true;
        }
    }

    public void setMethodAbove(String methodAbove) {
        this.methodAbove = methodAbove;
    }

    public String getMethodAbove() {
        return this.methodAbove;
    }

    public boolean isRenameMethod() {
        return isRenameMethod;
    }

    public boolean isMoveMethod() {
        return isMoveMethod;
    }

    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    public boolean isReplay() {
        return isReplay;
    }
}
