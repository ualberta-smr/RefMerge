package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.memberPullUp.PullUpProcessor;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

import java.util.List;

public class InvertPushDownMethod {

    Project project;

    public InvertPushDownMethod(Project project) {
        this.project = project;
    }

    public void invertPushDownMethod(RefactoringObject refactoringObject) {
        PushDownMethodObject pushDownMethodObject = (PushDownMethodObject) refactoringObject;
        String targetClassName = pushDownMethodObject.getTargetBaseClass();
        String targetClassFilePath = pushDownMethodObject.getDestinationFilePath();
        MethodSignatureObject pushedDownMethodObject = pushDownMethodObject.getOriginalMethodSignature();

        Utils utils = new Utils(project);
        utils.addSourceRoot(targetClassFilePath, targetClassName);

        // Get the target push down method class
        PsiClass targetPsiClass = utils.getPsiClassFromClassAndFileNames(targetClassName, targetClassFilePath);

        // If we cannot find the PSI class, do not try to replay the refactoring
        if(targetPsiClass == null) {
            return;
        }

        // Get the virtual file to refresh later
        VirtualFile vFile = targetPsiClass.getContainingFile().getVirtualFile();
        // Get the pushed down method to pull back up to base class
        PsiMethod psiMethod = Utils.getPsiMethod(targetPsiClass, pushedDownMethodObject);
        if(psiMethod == null) {
            return;
        }

        List<Pair<String, String>> subClasses = pushDownMethodObject.getSubClasses();
        MemberInfo[] memberInfos;
        // Get member for each class that method is pushed down
        memberInfos = utils.getMembersToPullUp(subClasses, pushedDownMethodObject);

        String sourceClassName = pushDownMethodObject.getOriginalClass();
        String sourceClassFilePath = pushDownMethodObject.getOriginalFilePath();

        PsiClass sourceClass = utils.getPsiClassFromClassAndFileNames(sourceClassName, sourceClassFilePath);

        // Pull up the pushed down methods from the target class to the original class
        PullUpProcessor pullUpProcessor = new PullUpProcessor(targetPsiClass, sourceClass, memberInfos, null);
        WriteCommandAction.runWriteCommandAction(project, pullUpProcessor::moveMembersToBase);
        WriteCommandAction.runWriteCommandAction(project, pullUpProcessor::moveFieldInitializations);

        utils.processMethodsDuplicates(pullUpProcessor);

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

        // Update the virtual file that contains the refactoring
        vFile.refresh(false, true);
    }



}
