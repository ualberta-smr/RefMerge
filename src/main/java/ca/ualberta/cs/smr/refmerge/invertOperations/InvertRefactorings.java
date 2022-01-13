package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

public class InvertRefactorings {
    /*
     * invertRefactorings takes a list of refactorings and performs the inverse for each one.
     */
    public static ArrayList<RefactoringObject> invertRefactorings(ArrayList<RefactoringObject> refactoringObjects,
                                                                  Project project) {
        // Iterate through the list of refactorings and undo each one
        for(RefactoringObject refactoringObject : refactoringObjects) {
            switch (refactoringObject.getRefactoringType()) {
                case RENAME_CLASS:
                case MOVE_CLASS:
                case MOVE_RENAME_CLASS:
                    try {
                        // Undo the rename class refactoring. This is commented out because of the prompt issue
                        InvertMoveRenameClass invertMoveRenameClass = new InvertMoveRenameClass(project);
                        invertMoveRenameClass.invertMoveRenameClass(refactoringObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case RENAME_METHOD:
                case MOVE_OPERATION:
                case MOVE_AND_RENAME_OPERATION:
                    // Undo the rename method refactoring
                    try {
                        InvertMoveRenameMethod invertMoveRenameMethod = new InvertMoveRenameMethod(project);
                        invertMoveRenameMethod.invertMoveRenameMethod(refactoringObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case EXTRACT_OPERATION:
                    try {
                        InvertExtractMethod invertExtractMethod = new InvertExtractMethod(project);
                        refactoringObject = invertExtractMethod.invertExtractMethod(refactoringObject);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    if(refactoringObject == null) {
                        break;
                    }
                    int index = refactoringObjects.indexOf(refactoringObject);
                    refactoringObjects.set(index, refactoringObject);
                    break;
                case INLINE_OPERATION:
                    try {
                        InvertInlineMethod invertInlineMethod = new InvertInlineMethod(project);
                        invertInlineMethod.invertInlineMethod(refactoringObject);
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
