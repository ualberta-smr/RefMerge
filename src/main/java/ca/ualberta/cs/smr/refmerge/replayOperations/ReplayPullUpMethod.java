package ca.ualberta.cs.smr.refmerge.replayOperations;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.refactoring.memberPullUp.PullUpProcessor;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.refactoring.util.duplicates.MethodDuplicatesHandler;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;
import com.intellij.util.Query;

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
        // Get member for each class that method is pulled up from
        memberInfos = getMembersToPullUp(subClasses, originalMethodObject, utils);





        String targetClassName = pullUpMethodObject.getTargetClass();
        String targetFile = pullUpMethodObject.getDestinationFilePath();

        PsiClass targetClass = utils.getPsiClassFromClassAndFileNames(targetClassName, targetFile);

        PullUpProcessor pullUpProcessor = new PullUpProcessor(sourceClass, targetClass, memberInfos, null);
        WriteCommandAction.runWriteCommandAction(project, pullUpProcessor::moveMembersToBase);
        WriteCommandAction.runWriteCommandAction(project, pullUpProcessor::moveFieldInitializations);

        processMethodsDuplicates(pullUpProcessor);

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }


        // Update the virtual file that contains the refactoring
        vFile.refresh(false, true);


    }

    private MemberInfo[] getMembersToPullUp(List<Pair<String, String>> subClasses, MethodSignatureObject methodObject, Utils utils) {
        MemberInfo[] psiMembers = new MemberInfo[subClasses.size()];

        int i = 0;
        for(Pair<String, String> subClass : subClasses) {
            String className = subClass.getFirst();
            String fileName = subClass.getSecond();
            PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(className, fileName);
            if(psiClass == null) {
                continue;
            }
            PsiMethod psiMethod = Utils.getPsiMethod(psiClass, methodObject);
            if(psiMethod == null) {
                continue;
            }
            psiMembers[i] = new MemberInfo(psiMethod);
            i++;
        }

        return  psiMembers;
    }


    private void processMethodsDuplicates(PullUpProcessor pullUpProcessor) {
        PsiClass myTargetSuperClass = pullUpProcessor.getTargetClass();
        Set<PsiMember> myMembersAfterMove = pullUpProcessor.getMovedMembers();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> ApplicationManager.getApplication().runReadAction(() -> {
            if (!myTargetSuperClass.isValid()) return;
            final Query<PsiClass> search = ClassInheritorsSearch.search(myTargetSuperClass);
            final Set<VirtualFile> hierarchyFiles = new HashSet<>();
            for (PsiClass aClass : search) {
                final PsiFile containingFile = aClass.getContainingFile();
                if (containingFile != null) {
                    final VirtualFile virtualFile = containingFile.getVirtualFile();
                    if (virtualFile != null) {
                        hierarchyFiles.add(virtualFile);
                    }
                }
            }
            final Set<PsiMember> methodsToSearchDuplicates = new HashSet<>();
            for (PsiMember psiMember : myMembersAfterMove) {
                if (psiMember instanceof PsiMethod && psiMember.isValid() && ((PsiMethod)psiMember).getBody() != null) {
                    methodsToSearchDuplicates.add(psiMember);
                }
            }

            MethodDuplicatesHandler.invokeOnScope(project, methodsToSearchDuplicates, new AnalysisScope(project, hierarchyFiles), true);
        }), MethodDuplicatesHandler.getRefactoringName(), true, project);
    }

}
