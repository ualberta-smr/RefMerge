package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameFieldObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

public class ReplayRenameField {

    Project project;

    public ReplayRenameField(Project project) {
        this.project = project;
    }

    public void replayRenameField(RefactoringObject ref) {
        RenameFieldObject renameFieldObject = (RenameFieldObject) ref;
        // The field name we are inverting to
        String originalField = renameFieldObject.getOriginalName();
        // The field name we are inverting
        String renamedField = renameFieldObject.getDestinationName();

        // The file and class that we are inverting the refactoring in. We use the original instead of the destination
        // because we replay the field refactorings before the class refactorings.
        String originalFile = renameFieldObject.getOriginalFilePath();
        String originalClass = renameFieldObject.getOriginalClass();

        Utils utils = new Utils(project);
        utils.addSourceRoot(originalFile, originalClass);

        PsiClass psiClass = null;
        // Set up for combining Rename Field, Move Field, and Rename + Move Field
        if(renameFieldObject.isRename()) {
            psiClass = utils.getPsiClassFromClassAndFileNames(originalClass, originalFile);

        }
        // If we cannot find the PSI class, do not invert the refactoring
        if(psiClass == null) {
            System.out.println("Could not find PSI Class for " + originalClass);
            return;
        }
        // Get the virtual file, so we can update the virtual file after performing the refactoring
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();

        // Get the PSI Field for the original field
        PsiField psiField = Utils.getPsiField(psiClass, originalField);

        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        assert psiField != null;
        // Rename the original field back to the refactored field
        RenameRefactoring renameRefactoring = factory.createRename(psiField, renamedField, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
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
