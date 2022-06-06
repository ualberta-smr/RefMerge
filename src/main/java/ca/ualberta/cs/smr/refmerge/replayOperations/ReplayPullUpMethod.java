package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.memberPullUp.PullUpProcessor;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

import java.util.*;

public class ReplayPullUpMethod {

    Project project;

    public ReplayPullUpMethod(Project project) {
        this.project = project;
    }


    public void replayPullUpMethod(RefactoringObject refactoringObject) {
        PullUpMethodObject pullUpMethodObject = (PullUpMethodObject) refactoringObject;
        String sourceClassName = pullUpMethodObject.getOriginalClass();
        String sourceFile = pullUpMethodObject.getOriginalFilePath();
        MethodSignatureObject originalMethodObject = pullUpMethodObject.getOriginalMethodSignature();

        Utils utils = new Utils(project);
        utils.addSourceRoot(sourceFile, sourceClassName);

        PsiClass sourceClass = utils.getPsiClassFromClassAndFileNames(sourceClassName, sourceFile);

        // If we cannot find the PSI class, do not try to replay the refactoring
        if(sourceClass == null) {
            return;
        }

        VirtualFile vFile = sourceClass.getContainingFile().getVirtualFile();
        PsiMethod psiMethod = Utils.getPsiMethod(sourceClass, originalMethodObject);
        if(psiMethod == null) {
            return;
        }

        List<Pair<String, String>> subClasses = pullUpMethodObject.getSubClasses();
        MemberInfo[] memberInfos;
        // Get member for each class that method is pushed down to
        memberInfos = utils.getMembersToPullUp(subClasses, originalMethodObject);





        String targetClassName = pullUpMethodObject.getTargetClass();
        String targetFile = pullUpMethodObject.getDestinationFilePath();

        PsiClass targetClass = utils.getPsiClassFromClassAndFileNames(targetClassName, targetFile);

        PullUpProcessor pullUpProcessor = new PullUpProcessor(sourceClass, targetClass, memberInfos, null);
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
