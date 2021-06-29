package ca.ualberta.cs.smr.core.refactoringObjects;

import org.refactoringminer.api.RefactoringType;

/*
 * The base refactoring object that contains getters and setters that each refactoring object needs.
 */
public interface RefactoringObject {

    RefactoringType getRefactoringType();

    RefactoringOrder getRefactoringOrder();

    void setOriginalFilePath(String originalFilePath);

    String getOriginalFilePath();

    void setDestinationFilePath(String destinationFilePath);

    String getDestinationFilePath();

    void setReplayFlag(boolean isReplay);

    boolean isReplay();

}
