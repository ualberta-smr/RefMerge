package ca.ualberta.cs.smr.core.refactoringObjects;

import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.ClassObject;
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
    private String originalFilePath;
    private String destinationFilePath;
    private ClassObject originalClassObject;
    private ClassObject destinationClassObject;
    private boolean isReplay;
    private boolean isRenameMethod;
    private boolean isMoveMethod;
    private boolean isSameFile;
    private boolean isMoveInner;
    private boolean isMoveOuter;


    /*
     * Use the provided information to create the rename class object for testing.
     */
    public MoveRenameClassObject(String originalFilePath, String originalClassName, String originalPackageName,
                                 String destinationFilePath, String destinationClassName, String destinationPackageName) {
        this.refactoringType = RefactoringType.RENAME_CLASS;
        this.originalFilePath = originalFilePath;
        this.destinationFilePath = destinationFilePath;
        this.originalClassObject =
                new ClassObject(originalClassName, originalPackageName);
        this.destinationClassObject =
                new ClassObject(destinationClassName, destinationPackageName);
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        this.isReplay = true;
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
        if((originalClass.isTopLevel() && !destinationClass.isTopLevel())
                || (!originalClass.isTopLevel() && !destinationClass.isTopLevel())) {
            this.isMoveInner = true;
        }
        // If an inner class is being refactored to a top level class
        else if(!originalClass.isTopLevel() && destinationClass.isTopLevel()) {
            this.isMoveOuter = true;
        }
        this.isMoveMethod = false;
        this.isRenameMethod = false;
        setType(refactoringType);
        this.isReplay = true;
    }

    public RefactoringType getRefactoringType() {
        return this.refactoringType;
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

    public void setOuterToInner() {
        this.isMoveInner = true;
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
