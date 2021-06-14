package ca.ualberta.cs.smr.core.refactoringObjects;

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
        this.isReplay = true;
    }

    /*
     * Creates the rename class object and takes the information that we need from the RefMiner refactoring object.
     */
    public MoveRenameClassObject(Refactoring refactoring) {
        RenameClassRefactoring renameClassRefactoring = (RenameClassRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        this.originalFilePath = renameClassRefactoring.getOriginalClass().getLocationInfo().getFilePath();
        this.destinationFilePath = renameClassRefactoring.getRenamedClass().getLocationInfo().getFilePath();
        this.originalClassObject =
                new ClassObject(renameClassRefactoring.getOriginalClassName(), renameClassRefactoring.getOriginalClass().getPackageName());
        this.destinationClassObject =
                new ClassObject(renameClassRefactoring.getRenamedClassName(), renameClassRefactoring.getRenamedClass().getPackageName());
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

    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    public boolean isReplay() {
        return isReplay;
    }

}
