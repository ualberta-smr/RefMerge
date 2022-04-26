package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveMembersRefactoring;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

public class InvertMoveRenameField {

    Project project;

    public InvertMoveRenameField(Project project) {
        this.project = project;
    }

    /*
     * Invert the move and rename field refactorings that was performed in the commit
     */
    public void invertRenameField(RefactoringObject ref) {
        MoveRenameFieldObject fieldObject = (MoveRenameFieldObject) ref;
        // The field name we are inverting to
        String originalField = fieldObject.getOriginalName();
        // The field name we are inverting
        String renamedField = fieldObject.getDestinationName();

        // The file and class that we are inverting the refactoring in. We use the original instead of the destination
        // because the class refactorings were already inverted.
        String originalFile = fieldObject.getOriginalFilePath();
        String originalClass = fieldObject.getOriginalClass();
        String destinationFile = fieldObject.getDestinationFilePath();
        String destinationClass = fieldObject.getDestinationClass();

        Utils utils = new Utils(project);
        utils.addSourceRoot(originalFile, originalClass);

        PsiClass psiClass = null;
        // If it is a Move Field or Rename+Move Field refactoring, use the destination class
        if(fieldObject.isMove()) {
            psiClass = utils.getPsiClassFromClassAndFileNames(destinationClass, destinationFile);
        }
        else if(fieldObject.isRename()) {
            psiClass = utils.getPsiClassFromClassAndFileNames(originalClass, originalFile);

        }

        // If we cannot find the PSI class, do not invert the refactoring
        if(psiClass == null) {
            System.out.println("Could not find PSI Class for " + originalClass);
            return;
        }
        // Get the virtual file, so we can update the virtual file after performing the refactoring
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();

        PsiField psiField = Utils.getPsiField(psiClass, renamedField);
        // If the field object is a rename field, perform the rename field first
        if(fieldObject.isRename()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            assert psiField != null;
            RenameRefactoring renameRefactoring = factory.createRename(psiField, originalField, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        // Now, if the field was moved, undo the move by performing a move field refactoring to move it to the original class
        if(fieldObject.isMove()) {
            JavaRefactoringFactory refactoringFactory = JavaRefactoringFactory.getInstance(project);
            String visibility = fieldObject.getVisibility();
            PsiMember[] psiMembers = new PsiMember[1];
            psiMembers[0] = psiField;
            MoveMembersRefactoring moveFieldRefactoring = refactoringFactory.createMoveMembers(psiMembers,
                    originalClass, visibility);
            UsageInfo[] refactoringUsages = moveFieldRefactoring.findUsages();
            moveFieldRefactoring.doRefactoring(refactoringUsages);
            psiClass = moveFieldRefactoring.getTargetClass();
            if(psiClass == null) {
                return;
            }
        }

        // Check if there is a usage view. if so, close the usage view and do not perform the refactoring.
        // Need to check if there is a workaround or solution for this
        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

        // Update the virtual file that contains the refactoring
        virtualFile.refresh(false, true);

    }
}
