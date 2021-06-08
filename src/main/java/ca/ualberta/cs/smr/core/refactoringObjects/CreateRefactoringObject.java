package ca.ualberta.cs.smr.core.refactoringObjects;

import org.refactoringminer.api.Refactoring;


/*
 * Creates the refactoring object from the RefMiner refactoring object.
 */
public class CreateRefactoringObject {

    public static RefactoringObject createRefactoringObject(Refactoring refactoring) {
        switch(refactoring.getRefactoringType()) {
            case RENAME_CLASS:
                return new RenameClassObject(refactoring);
            case RENAME_METHOD:
                return new RenameMethodObject(refactoring);
            case EXTRACT_OPERATION:
                return new ExtractMethodObject(refactoring);

        }
        return null;
    }
}
