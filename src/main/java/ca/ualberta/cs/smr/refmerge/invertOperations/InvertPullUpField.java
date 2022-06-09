package ca.ualberta.cs.smr.refmerge.invertOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.memberPushDown.PushDownProcessor;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

import java.util.ArrayList;
import java.util.List;

public class InvertPullUpField {

    private final Project project;

    public InvertPullUpField(Project project) {
        this.project = project;
    }

    public void invertPullUpField(RefactoringObject refactoringObject) {

        PullUpFieldObject pullUpFieldObject = (PullUpFieldObject) refactoringObject;
        String superClassName = pullUpFieldObject.getTargetClass();
        String superClassFile = pullUpFieldObject.getDestinationFilePath();
        String refactored = pullUpFieldObject.getOriginalFieldName();

        Utils utils = new Utils(project);
        utils.addSourceRoot(superClassFile, superClassName);

        PsiClass superClass;
        // Use super class name and super class file path to get the class that the field is pulled up to
        superClass = utils.getPsiClassFromClassAndFileNames(superClassName, superClassFile);

        // If we cannot find the PSI class, do not try to invert the refactoring
        if(superClass == null) {
            return;
        }

        VirtualFile vFile = superClass.getContainingFile().getVirtualFile();
        // Get the PSI field using the PSI super class and destination field name
        PsiField psiField = Utils.getPsiField(superClass, refactored);
        if(psiField == null) {
            return;
        }

        List<MemberInfo> memberInfos = new ArrayList<>();
        // Create the member info for the refactored field
        MemberInfo memberInfo = new MemberInfo(psiField);
        memberInfos.add(memberInfo);
        // Use as is policy for now
        DocCommentPolicy<PsiComment> policy = new DocCommentPolicy<>(DocCommentPolicy.ASIS);
        PushDownProcessor<MemberInfo, PsiMember, PsiClass> pushDownProcessor = new PushDownProcessor<>(superClass, memberInfos, policy);


        List<Pair<String,String>> subClasses = pullUpFieldObject.getSubClasses();
        UsageInfo[] usageInfos = new UsageInfo[subClasses.size()];
        // Get the list of subclasses that the field is pulled up from
        for(int i = 0; i < usageInfos.length; i++) {
            Pair<String, String> subClass = subClasses.get(i);
            String className = subClass.getFirst();
            String fileName = subClass.getSecond();
            PsiClass psiSubClass = utils.getPsiClassFromClassAndFileNames(className, fileName);
            // If the class cannot be found, skip the given class
            if(psiSubClass == null) {
                continue;
            }
            UsageInfo usageInfo = new UsageInfo(psiSubClass, false);
            usageInfos[i] = usageInfo;
        }
        // Push down the field to the classes it was pulled up from
        WriteCommandAction.runWriteCommandAction(project, () -> pushDownProcessor.pushDownToClasses(usageInfos));
        // Delete target field
        WriteCommandAction.runWriteCommandAction(project, psiField::delete);


        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }


        // Update the virtual file that contains the refactoring
        vFile.refresh(false, true);

    }

}
