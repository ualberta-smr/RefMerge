package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
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
                case RENAME_ATTRIBUTE:
                case MOVE_ATTRIBUTE:
                case MOVE_RENAME_ATTRIBUTE:
                    try {
                        ReplayMoveRenameField replayMoveRenameField = new ReplayMoveRenameField(project);
                        replayMoveRenameField.replayRenameField(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                case PULL_UP_OPERATION:
                    try {
                        ReplayPullUpMethod replayPullUpMethod = new ReplayPullUpMethod(project);
                        replayPullUpMethod.replayPullUpMethod(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                case PUSH_DOWN_OPERATION:
                    try {
                        ReplayPushDownMethod replayPushDownMethod = new ReplayPushDownMethod(project);
                        replayPushDownMethod.replayPushDownMethod(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                case PULL_UP_ATTRIBUTE:
                    try {
                        ReplayPullUpField replayPullUpField = new ReplayPullUpField(project);
                        replayPullUpField.replayPullUpField(refactoringObject);
                    } catch(Exception exception) {
                        exception.printStackTrace();
                    }
                case PUSH_DOWN_ATTRIBUTE:
                    try {
                        ReplayPushDownField replayPushDownField = new ReplayPushDownField(project);
                        replayPushDownField.replayPushDownField(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                case RENAME_PACKAGE:
                    try {
                        ReplayRenamePackage replayRenamePackage = new ReplayRenamePackage(project);
                        replayRenamePackage.replayRenamePackage(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
            }

        }

        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
    }

}
