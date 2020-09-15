package ca.ualberta.cs.smr.core;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.refactoringminer.api.Refactoring;

public class UndoOperations {

    Project proj;

    public UndoOperations(Project proj) {
        this.proj = proj;
    }

    public void undoRenameMethod(Refactoring ref, Project project) {

        String refS = ref.toString();
        System.out.println(refS);
        String destName = refS.substring(refS.indexOf("to") + 3, refS.indexOf("(", refS.indexOf("(") + 1));
        destName = destName.substring(destName.indexOf(" ") + 1, destName.length());
        String srcName = refS.substring(refS.indexOf("\t"), refS.indexOf("("));
        srcName = srcName.split(" ")[1];
        String qualifiedClass = refS.substring(refS.indexOf("class ") + 6, refS.length());
        String qClass = qualifiedClass.substring(qualifiedClass.lastIndexOf('.') + 1, qualifiedClass.length());

        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        System.out.println(proj.getBasePath());
        PsiClass jClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));
        // If the qualified class name couldn't be found, try using the class name as file name and find that file
        RenameProcessor processor = null;
        if (jClass == null) {
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
                if (method.getName().equals(destName)) {
                    System.out.println("Method Name: " + method.getName());
                    processor = new RenameProcessor(proj, method, srcName, false, false);
                    RenameProcessor finalProcessor = processor;
                    ApplicationManager.getApplication().invokeAndWait(() -> finalProcessor.doRun(), ModalityState.current());

                    VirtualFile vFile = pFile.getVirtualFile();
                    vFile.refresh(false, true);
                    break;
                }
            }
        }
        // If the class is found
        else {
            System.out.println("Class: " + qualifiedClass);
            PsiMethod[] methods = jClass.getMethods();
            for (PsiMethod method : methods) {
                if (method.getName().equals(destName)) {
                    System.out.println("Method Name: " + method.getName());
                    processor = new RenameProcessor(proj, method, srcName, false, false);
                    RenameProcessor finalProcessor = processor;
                    ApplicationManager.getApplication().invokeAndWait(() -> finalProcessor.doRun(), ModalityState.current());
                    VirtualFile vFile = jClass.getContainingFile().getVirtualFile();
                    vFile.refresh(false, true);
                    break;
                }
            }
        }

    }

    public void undoRenameClass(Refactoring ref) {
        System.out.println(ref.toString());
        String srcClass = ((RenameClassRefactoring) ref).getOriginalClassName();
        String renamedClass = ((RenameClassRefactoring) ref).getRenamedClassName();
        String srcClassName = srcClass.substring(srcClass.lastIndexOf(".") + 1).trim();
        String renamedClassName = renamedClass.substring(renamedClass.lastIndexOf(".") + 1).trim();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        PsiClass jClass = jPF.findClass(renamedClass, GlobalSearchScope.allScope((proj)));
        if(jClass == null) {
            String qClass = renamedClassName + ".java";
            System.out.println(qClass);
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, qClass, GlobalSearchScope.allScope(proj));
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            PsiClass[] jClasses = pFile.getClasses();
            for(PsiClass psiClass : jClasses) {
                if(psiClass.getQualifiedName().equals(renamedClass)) {
                    RenameProcessor processor = new RenameProcessor(proj, psiClass, srcClassName, true, true);
                    RenameProcessor finalProcessor = processor;
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(() -> finalProcessor.run(), ModalityState.current());
                    VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
                    vFile.refresh(false, true);
                }

            }
        }
        else {
            RenameProcessor proc = new RenameProcessor(proj, jClass, srcClassName, false, false);
            proc.run();
            VirtualFile vFile = jClass.getContainingFile().getVirtualFile();
            vFile.refresh(false, true);
        }
    }




}
