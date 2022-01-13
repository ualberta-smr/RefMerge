package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import org.refactoringminer.api.RefactoringType;

/*
 * The base refactoring object that contains getters and setters that each refactoring object needs.
 */
public interface RefactoringObject {

    void setStartLine(int startLine);

    void setEndLine(int endLine);

    int getStartLine();

    int getEndLine();

    String getRefactoringDetail();

    RefactoringType getRefactoringType();

    RefactoringOrder getRefactoringOrder();

    void setOriginalFilePath(String originalFilePath);

    String getOriginalFilePath();

    void setDestinationFilePath(String destinationFilePath);

    String getDestinationFilePath();

    void setReplayFlag(boolean isReplay);

    boolean isReplay();

}
