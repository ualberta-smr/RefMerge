package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import org.refactoringminer.api.Refactoring;


/*
 * Creates the refactoring object from the RefMiner refactoring object.
 */
public class RefactoringObjectUtils {

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
