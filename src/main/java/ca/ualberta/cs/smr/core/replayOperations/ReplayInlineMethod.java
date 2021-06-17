package ca.ualberta.cs.smr.core.replayOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.project.Project;

public class ReplayInlineMethod {

    Project project;

    public ReplayInlineMethod(Project project) {
        this.project = project;
    }

    /*
     * Replay the inline method refactoring by performing an inline method refactoring.
     */
    public void replayInlineMethod(RefactoringObject ref) {

    }
}
