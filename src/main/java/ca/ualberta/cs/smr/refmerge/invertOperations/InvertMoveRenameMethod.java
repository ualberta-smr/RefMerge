package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.*;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

public class InvertMoveRenameMethod {

    Project project;

    public InvertMoveRenameMethod(Project project) {
        this.project = project;
    }

    /*
     * Invert the rename method refactoring that was performed in the commit
     */
    public void invertMoveRenameMethod(RefactoringObject ref) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) ref;
        MethodSignatureObject original = moveRenameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject refactored = moveRenameMethodObject.getDestinationMethodSignature();
        String originalMethodName = original.getName();
        String originalClassName = moveRenameMethodObject.getOriginalClassName();
        // String destinationClassName = moveRenameMethodObject.getDestinationClassName();
        String destinationClassName = moveRenameMethodObject.getOriginalDestinationClassName();
        // get the PSI class using original the qualified class name
        String filePath = moveRenameMethodObject.getDestinationFilePath();
        Utils utils = new Utils(project);
        utils.addSourceRoot(filePath, destinationClassName);
        PsiClass psiClass;
        if(moveRenameMethodObject.isMoveMethod()) {
            psiClass = utils.getPsiClassFromClassAndFileNames(destinationClassName, filePath);
        }
        else {
            psiClass = utils.getPsiClassFromClassAndFileNames(originalClassName, filePath);
        }
        // If we cannot find the PSI class, do not try to invert the refactoring
        if(psiClass == null) {
            return;
        }
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, refactored);
        if(psiMethod == null) {
            return;
        }

        // If the operation was refactored, undo the method refactoring by performing a method refactoring to change it
        // to the original operation
        if(moveRenameMethodObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiMethod, originalMethodName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);

        }
        // If the operation was moved, undo the move method by performing a move method refactoring to move it to the
        // original class
        if(moveRenameMethodObject.isMoveMethod()) {
            // Get the method before the moved method so it can be moved to the correct location
            moveRenameMethodObject.setMethodAbove(getAboveMethodBeforeMove(psiClass, psiMethod));

            JavaRefactoringFactory refactoringFactory = JavaRefactoringFactory.getInstance(project);
            String visibility = original.getVisibility();
            PsiMember[] psiMembers = new PsiMember[1];
            psiMembers[0] = psiMethod;
            MoveMembersRefactoring moveMethodRefactoring = refactoringFactory.createMoveMembers(psiMembers,
                    originalClassName, visibility);
            UsageInfo[] refactoringUsages = moveMethodRefactoring.findUsages();
            moveMethodRefactoring.doRefactoring(refactoringUsages);
            psiClass = moveMethodRefactoring.getTargetClass();
            if(psiClass == null) {
                return;
            }
        }

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

        // Update the virtual file that contains the refactoring
        vFile.refresh(false, true);

    }

    /*
     * Get the method signature before the method that's moved so we can move it back to the same spot.
     */

    private String getAboveMethodBeforeMove(PsiClass psiClass, PsiMethod psiMethod) {
        String signatureString = null;
        PsiMethod[] psiMethods = psiClass.getMethods();
        for(int i = 0; i < psiMethods.length; i++) {

            PsiMethod otherMethod = psiMethods[i];
            if(psiMethod.getSignature(PsiSubstitutor.UNKNOWN).equals(otherMethod.getSignature(PsiSubstitutor.UNKNOWN))) {
                if(i == 0) {
                    break;
                }
                signatureString = psiMethods[i-1].getSignature(PsiSubstitutor.UNKNOWN).toString();
                break;
            }
        }
        return signatureString;
    }

}
