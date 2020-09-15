package ca.ualberta.cs.smr.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import org.refactoringminer.api.Refactoring;

public class ReplayOperations {

    Project proj;

    public ReplayOperations(Project proj) {
        this.proj = proj;
    }


    public void replayRenameMethod(Refactoring ref) {
        String refS = ref.toString();
        String destName = refS.substring(refS.indexOf("to") + 3, refS.indexOf("(", refS.indexOf("(") + 1));
        destName = destName.substring(destName.indexOf(" ") + 1, destName.length());
        String srcName = refS.substring(refS.indexOf("\t"), refS.indexOf("("));
        srcName = srcName.split(" ")[1];
        String qualifiedClass = refS.substring(refS.indexOf("class ") + 6, refS.length());
        String qClass = qualifiedClass.substring(qualifiedClass.lastIndexOf('.') + 1, qualifiedClass.length());
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        DumbService.isDumb(proj);
        PsiClass jClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));
        System.out.println(qualifiedClass);
        RenameProcessor processor = null;
        if(jClass == null) {
            System.out.println("Thing : " + qClass);
            qClass = qClass + ".java";
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, qClass, GlobalSearchScope.allScope(proj));
            if(pFiles.length == 0) {
                System.out.println("FAILED HERE");
                System.out.println(qClass);
                System.out.println(srcName);
                return;
            }
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            PsiClass[] jClasses = pFile.getClasses();
            for (PsiClass it : jClasses) {
                System.out.println(it.getQualifiedName());
                if (it.getQualifiedName().equals(qualifiedClass)) {
                    jClass = it;
                }
            }
            PsiMethod[] methods = jClass.getMethods();

            for (PsiMethod method : methods) {
                if (method.getName().equals(srcName)) {
                    System.out.println("Method Name: " + method.getName());
                    processor = new RenameProcessor(proj, method, destName, false, false);
                    RenameProcessor finalProcessor = processor;
                    ApplicationManager.getApplication().invokeAndWait(() -> finalProcessor.doRun(), ModalityState.current());
                    VirtualFile vFile = pFile.getVirtualFile();
                    vFile.refresh(false, true);
                    break;
                }
            }
        }
        else {
            PsiMethod[] methods = jClass.getMethods();
            for (PsiMethod method : methods) {
                if (method.getName().equals(srcName)) {
                    System.out.println("Method Name: " + method.getName());
                    processor = new RenameProcessor(proj, method, destName, false, false);
                    RenameProcessor finalProcessor = processor;
                    ApplicationManager.getApplication().invokeAndWait(() -> finalProcessor.doRun(), ModalityState.current());
                    VirtualFile vFile = jClass.getContainingFile().getVirtualFile();
                    vFile.refresh(false, true);
                    break;
                }
            }
        }



    }

}
