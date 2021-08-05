package ca.ualberta.cs.smr.core.undoOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

public class UndoRefactorings {
    /*
     * undoRefactorings takes a list of refactorings and performs the inverse for each one.
     */
    public static ArrayList<RefactoringObject> undoRefactorings(ArrayList<RefactoringObject> refactoringObjects,
                                                                Project project) {
        // Iterate through the list of refactorings and undo each one
        for(RefactoringObject refactoringObject : refactoringObjects) {
            switch (refactoringObject.getRefactoringType()) {
                case RENAME_CLASS:
                case MOVE_CLASS:
                case MOVE_RENAME_CLASS:
                    try {
                        // Undo the rename class refactoring. This is commented out because of the prompt issue
                        UndoMoveRenameClass undoMoveRenameClass = new UndoMoveRenameClass(project);
                        undoMoveRenameClass.undoMoveRenameClass(refactoringObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case RENAME_METHOD:
                case MOVE_OPERATION:
                case MOVE_AND_RENAME_OPERATION:
                    // Undo the rename method refactoring
                    try {
                        UndoMoveRenameMethod undoMoveRenameMethod = new UndoMoveRenameMethod(project);
                        undoMoveRenameMethod.undoMoveRenameMethod(refactoringObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case EXTRACT_OPERATION:
                    UndoExtractMethod undoExtractMethod = new UndoExtractMethod(project);
                    refactoringObject = undoExtractMethod.undoExtractMethod(refactoringObject);
                    if(refactoringObject == null) {
                        continue;
                    }
                    int index = refactoringObjects.indexOf(refactoringObject);
                    refactoringObjects.set(index, refactoringObject);
                    break;
                case INLINE_OPERATION:
                    try {
                        UndoInlineMethod undoInlineMethod = new UndoInlineMethod(project);
                        undoInlineMethod.undoInlineMethod(refactoringObject);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;

            }

        }
        // Save all of the refactoring changes from memory onto disk
        FileDocumentManager.getInstance().saveAllDocuments();
        return refactoringObjects;
    }
}
