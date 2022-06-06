package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
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

public class InvertPushDownField {

    Project project;

    public InvertPushDownField(Project project) {
        this.project = project;
    }

    /*
     * Invert push down field by performing a pull up refactoring
     */
    public void invertPushDownField(RefactoringObject refactoringObject) {
        PushDownFieldObject pushDownFieldObject = (PushDownFieldObject) refactoringObject;
        String targetSubClass = pushDownFieldObject.getTargetSubClass();
        String targetFile = pushDownFieldObject.getDestinationFilePath();
        String movedFieldName = pushDownFieldObject.getOriginalFieldName();

        Utils utils = new Utils(project);
        utils.addSourceRoot(targetFile, targetSubClass);

        // Get target PSI subclass using class name and file path
        PsiClass targetClass = utils.getPsiClassFromClassAndFileNames(targetSubClass, targetFile);

        // If we cannot find the PSI class, do not try to invert the refactoring
        if(targetClass == null) {
            return;
        }

        VirtualFile vFile = targetClass.getContainingFile().getVirtualFile();
        PsiField psiField = Utils.getPsiField(targetClass, movedFieldName);
        if(psiField == null) {
            return;
        }

        List<Pair<String, String>> subClasses = pushDownFieldObject.getSubClasses();
        MemberInfo[] memberInfos;
        // Get member for each class that field is pulled up from
        memberInfos = utils.getFieldsToPullUp(subClasses, movedFieldName);




        // Get the class that the fields are pushed down from
        String sourceClassName = pushDownFieldObject.getOriginalClass();
        String sourceFile = pushDownFieldObject.getOriginalFilePath();

        PsiClass sourceClass = utils.getPsiClassFromClassAndFileNames(sourceClassName, sourceFile);

        // Pull up fields from target classes to the original source class
        PullUpProcessor pullUpProcessor = new PullUpProcessor(targetClass, sourceClass, memberInfos, null);
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
