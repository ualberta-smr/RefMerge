package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveMembersRefactoring;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;

public class ReplayMoveRenameMethod {

    Project project;

    public ReplayMoveRenameMethod(Project project) {
        this.project = project;
    }

    /*
     * replayRenameMethod performs the rename method refactoring.
     */
    public void replayMoveRenameMethod(RefactoringObject ref) {
        MoveRenameMethodObject moveRenameMethodObject = (MoveRenameMethodObject) ref;
        MethodSignatureObject original = moveRenameMethodObject.getOriginalMethodSignature();
        MethodSignatureObject renamed = moveRenameMethodObject.getDestinationMethodSignature();
        String destinationMethodName = renamed.getName();
        String originalClassName = moveRenameMethodObject.getOriginalClassName();
        String destinationClassName = moveRenameMethodObject.getOriginalDestinationClassName();
        String filePath = moveRenameMethodObject.getOriginalFilePath();
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(originalClassName, filePath);
        // If we fail to find the PSI class, do not try to replay
        if(psiClass == null) {
            return;
        }
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, original);
        if(psiMethod == null) {
            return;
        }
        if(moveRenameMethodObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiMethod, destinationMethodName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        if(moveRenameMethodObject.isMoveMethod()) {
            JavaRefactoringFactory refactoringFactory = JavaRefactoringFactory.getInstance(project);
            String visibility = original.getVisibility();
            PsiMember[] psiMembers = new PsiMember[1];
            psiMembers[0] = psiMethod;
            MoveMembersRefactoring moveMethodRefactoring = refactoringFactory.createMoveMembers(psiMembers,
                    destinationClassName, visibility);
            UsageInfo[] refactoringUsages = moveMethodRefactoring.findUsages();
            moveMethodRefactoring.doRefactoring(refactoringUsages);
            psiClass = moveMethodRefactoring.getTargetClass();
            moveMethodToOriginallyMovedLocation(psiClass, psiMethod, moveRenameMethodObject.getMethodAbove());
        }
        // Update the virtual file containing the refactoring
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
    }

    /*
     * Move the method to the correct location within the class using the method signature of the above method.
     */
    private void moveMethodToOriginallyMovedLocation(PsiClass psiClass, PsiMethod psiMethod,  String aboveSignature) {

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        // Get all of the methods inside of the class.
        PsiMethod[] psiMethods = psiClass.getMethods();

        // Get the physical copy of the PSI method so we can delete it
        for (PsiMethod method : psiMethods) {
            boolean isSame = true;
            String m1 = method.getName();
            String m2 = psiMethod.getName();
            if(m1.equals(m2)) {
                PsiParameter[] parameterList1 = method.getParameterList().getParameters();
                PsiParameter[] parameterList2 = psiMethod.getParameterList().getParameters();
                if(parameterList1.length != parameterList2.length) {
                    continue;
                }
                for(int i = 0; i < parameterList1.length; i++) {
                    if (!parameterList1[i].isEquivalentTo(parameterList2[i])) {
                        isSame = false;
                        break;
                    }
                }
            }
            if (isSame) {
                psiMethod = method;
                break;
            }
        }


        // Create the new PSI method
        final PsiMethod newMethod = PsiElementFactory.getInstance(project).createMethodFromText(psiMethod.getText(), psiClass);
        PsiMethod finalPsiMethod = psiMethod;
        // If the method is the first method in the class
        if(aboveSignature == null) {
            PsiMethod psiMethodAfter = null;
            for(PsiMethod otherMethod : psiMethods) {
                if(!otherMethod.isConstructor()) {
                    psiMethodAfter = otherMethod;
                    break;
                }
            }
            PsiMethod finalPsiMethodAfter = psiMethodAfter;
            WriteCommandAction.runWriteCommandAction(project, () -> {
                psiClass.addBefore(newMethod, finalPsiMethodAfter);
                finalPsiMethod.delete();
            });
            return;
        }

        PsiMethod psiMethodBefore = null;
        // Find which method comes before the moved method
        for(PsiMethod otherMethod : psiMethods) {
            if(otherMethod.getSignature(PsiSubstitutor.UNKNOWN).toString().equals(aboveSignature)) {
                psiMethodBefore = otherMethod;
                break;
            }
        }
        PsiMethod finalPsiMethodBefore = psiMethodBefore;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiClass.addAfter(newMethod, finalPsiMethodBefore);
            finalPsiMethod.delete();
        });
    }


}
