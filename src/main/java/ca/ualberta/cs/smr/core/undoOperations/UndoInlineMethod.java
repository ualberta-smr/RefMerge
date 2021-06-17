package ca.ualberta.cs.smr.core.undoOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class UndoInlineMethod {

    Project project;

    public UndoInlineMethod(Project project) {
        this.project = project;
    }

    /*
     * Undo the inline method refactoring that was originally performed by performing an extract method refactoring.
     */
    public void undoInlineMethod(RefactoringObject ref) {

    }
}
