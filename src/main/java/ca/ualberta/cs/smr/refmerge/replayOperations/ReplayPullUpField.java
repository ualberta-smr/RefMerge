package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.refactoring.memberPullUp.PullUpProcessor;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

import java.util.List;

public class ReplayPullUpField {

    Project project;

    public ReplayPullUpField(Project project) {
        this.project = project;
    }

    public void replayPullUpField(RefactoringObject refactoringObject) {
        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) refactoringObject;
        String sourceClassName = pullUpFieldObject.getOriginalClass();
        String sourceFile = pullUpFieldObject.getOriginalFilePath();
        String originalFieldName = pullUpFieldObject.getOriginalFieldName();

        Utils utils = new Utils(project);
        utils.addSourceRoot(sourceFile, sourceClassName);

        PsiClass sourceClass = utils.getPsiClassFromClassAndFileNames(sourceClassName, sourceFile);

        // If we cannot find the PSI class, do not try to replay the refactoring
        if(sourceClass == null) {
            return;
        }

        VirtualFile vFile = sourceClass.getContainingFile().getVirtualFile();
        PsiField psiField = Utils.getPsiField(sourceClass, originalFieldName);
        if(psiField == null) {
            return;
        }

        List<Pair<String, String>> subClasses = pullUpFieldObject.getSubClasses();
        MemberInfo[] memberInfos;
        // Get member for each class that field is pulled up from
        memberInfos = utils.getFieldsToPullUp(subClasses, originalFieldName);





        String targetClassName = pullUpFieldObject.getTargetClass();
        String targetFile = pullUpFieldObject.getDestinationFilePath();

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
