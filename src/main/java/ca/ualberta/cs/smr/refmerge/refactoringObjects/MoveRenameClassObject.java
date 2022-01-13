package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ClassObject;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.diff.MoveAndRenameClassRefactoring;
import gr.uom.java.xmi.diff.MoveClassRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;


/*
 * Represents a rename class refactoring. Contains the necessary information for logic checks and performing the
 * refactoring using the IntelliJ refactoring engine.
 */
public class MoveRenameClassObject implements RefactoringObject {

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
     * Use the provided information to create the rename class object for testing.
     */
    public MoveRenameClassObject(String originalFilePath, String originalClassName, String originalPackageName,
                                 String destinationFilePath, String destinationClassName, String destinationPackageName) {
        this.refactoringType = RefactoringType.RENAME_CLASS;
        this.refactoringDetail = "";
        this.originalFilePath = originalFilePath;
        this.destinationFilePath = destinationFilePath;
        this.originalClassObject =
                new ClassObject(originalClassName, originalPackageName);
        this.destinationClassObject =
                new ClassObject(destinationClassName, destinationPackageName);
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        this.isReplay = true;
        this.isSameFile = false;
        this.isMoveOuter = false;
        this.isMoveInner = false;
        this.isMoveInnerToInner = false;
        this.startLine = 0;
        this.endLine = 0;
    }

    /*
     * Creates the rename class object and takes the information that we need from the RefMiner refactoring object.
     */
    public MoveRenameClassObject(Refactoring refactoring) {
        UMLClass originalClass;
        UMLClass destinationClass;
        if(refactoring instanceof RenameClassRefactoring) {
            RenameClassRefactoring renameClassRefactoring = (RenameClassRefactoring) refactoring;
            originalClass = renameClassRefactoring.getOriginalClass();
            destinationClass = renameClassRefactoring.getRenamedClass();

        }
        else if(refactoring instanceof MoveClassRefactoring) {
            MoveClassRefactoring moveClassRefactoring = (MoveClassRefactoring) refactoring;
            originalClass = moveClassRefactoring.getOriginalClass();
            destinationClass = moveClassRefactoring.getMovedClass();
        }
        else {
            MoveAndRenameClassRefactoring moveAndRenameClassRefactoring = (MoveAndRenameClassRefactoring) refactoring;
            originalClass = moveAndRenameClassRefactoring.getOriginalClass();
            destinationClass = moveAndRenameClassRefactoring.getRenamedClass();

        }
        this.refactoringType = refactoring.getRefactoringType();
        this.originalFilePath = originalClass.getLocationInfo().getFilePath();
        this.destinationFilePath = destinationClass.getLocationInfo().getFilePath();
        this.originalClassObject =
                new ClassObject(originalClass.getName(), originalClass.getPackageName());
        this.destinationClassObject =
                new ClassObject(destinationClass.getName(), destinationClass.getPackageName());
        this.isMoveInner = false;
        this.isMoveOuter = false;
        // If the original and destination file path are the same, then the outer/inner class refactoring was performed
        // in the same file.
        this.isSameFile = originalFilePath.equals(destinationFilePath);

        // If the move/move+class refactoring is moving a top level or inner class to an inner class
        if(!originalClass.isTopLevel() && !destinationClass.isTopLevel()) {
            this.isMoveInnerToInner = true;
            this.startOffset = originalClass.getLocationInfo().getStartOffset();
        } else if (originalClass.isTopLevel() && !destinationClass.isTopLevel()) {
            this.isMoveInner = true;
            this.startOffset = originalClass.getLocationInfo().getStartOffset();
        }
        // If an inner class is being refactored to a top level class
        else if (!originalClass.isTopLevel() && destinationClass.isTopLevel()) {
            this.isMoveOuter = true;
            this.startOffset = originalClass.getLocationInfo().getStartOffset();
        }
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        setType(refactoringType);
        this.refactoringDetail = refactoring.toString();
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

    public RefactoringType getRefactoringType() {
        return this.refactoringType;
    }

    public String getRefactoringDetail() {
        return this.refactoringDetail;
    }

    public RefactoringOrder getRefactoringOrder() {
        return RefactoringOrder.MOVE_RENAME_CLASS;
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

    public void setOriginalClassObject(ClassObject originalClassObject) {
        this.originalClassObject = originalClassObject;
    }

    public ClassObject getOriginalClassObject() {
        return originalClassObject;
    }

    public void setDestinationClassObject(ClassObject destinationClassObject) {
        this.destinationClassObject = destinationClassObject;
    }

    public ClassObject getDestinationClassObject() {
        return destinationClassObject;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setType(RefactoringType refactoringType) {
        if(refactoringType.equals(RefactoringType.RENAME_CLASS)) {
            this.isRenameMethod = true;
        }
        else if(refactoringType.equals(RefactoringType.MOVE_CLASS)) {
            this.isMoveMethod = true;
        }
        else {
            this.isRenameMethod = true;
            this.isMoveMethod = true;
        }
    }

    public void setInnerToOuter() {
        this.isMoveOuter = true;
    }

    public void setOuterToInner() {
        this.isMoveInner = true;
    }

    public void setInnerToInner() {
        this.isMoveInnerToInner = true;
    }

    public void setSameFile() {
        this.isSameFile = true;
    }

    public boolean isSameFile() {
        return isSameFile;
    }

    public boolean isMoveInner() {
        return isMoveInner;
    }

    public boolean isMoveInnerToInner() {
        return isMoveInnerToInner;
    }

    public boolean isMoveOuter() {
        return isMoveOuter;
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
