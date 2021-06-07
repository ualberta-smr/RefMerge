package ca.ualberta.cs.smr.core.refactoringObjects;

import org.refactoringminer.api.RefactoringType;

public interface RefactoringObject {

    RefactoringType getRefactoringType();

    void setOriginalFilePath(String originalFilePath);

    String getOriginalFilePath();

    void setDestinationFilePath(String destinationFilePath);

    String getDestinationFilePath();

}
