package ca.ualberta.cs.smr.core.replayOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

public class ReplayRefactorings {

    /*
     * replayRefactorings takes a list of refactorings and performs each of the refactorings.
     */
    public static void replayRefactorings(ArrayList<RefactoringObject> refactoringObjects, Project project) {
        for(RefactoringObject refactoringObject : refactoringObjects) {
            switch (refactoringObject.getRefactoringType()) {
                case RENAME_CLASS:
                case MOVE_CLASS:
                case MOVE_RENAME_CLASS:
                    try {
                        ReplayMoveRenameClass replayMoveRenameClass = new ReplayMoveRenameClass(project);
                        replayMoveRenameClass.replayMoveRenameClass(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                case RENAME_METHOD:
                case MOVE_OPERATION:
                case MOVE_AND_RENAME_OPERATION:
                    try {
                        ReplayMoveRenameMethod replayMoveRenameMethod = new ReplayMoveRenameMethod(project);
                        replayMoveRenameMethod.replayMoveRenameMethod(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                case EXTRACT_OPERATION:
                    ReplayExtractMethod replayExtractMethod = new ReplayExtractMethod(project);
                    try {
                        replayExtractMethod.replayExtractMethod(refactoringObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case INLINE_OPERATION:
                    try {
                        ReplayInlineMethod replayInlineMethod = new ReplayInlineMethod(project);
                        replayInlineMethod.replayInlineMethod(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
            }

        }

        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

}
