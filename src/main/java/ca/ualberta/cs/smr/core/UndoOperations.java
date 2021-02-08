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
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

public class UndoOperations {

    Project proj;

    public UndoOperations(Project proj) {
        this.proj = proj;
    }

    /*
     * Undo the rename method refactoring that was performed in the commit
     */
    public void undoRenameMethod(Refactoring ref) {
        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        // Get the original method name
        String srcName = original.getName();
        // Get the refactored method name
        String destName = renamed.getName();
        String qualifiedClass = original.getClassName();
        String className = original.getNonQualifiedClassName();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        // get the PSI class using the qualified class name
        PsiClass jClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));
        RenameProcessor processor;
        // If the qualified class name couldn't be found, try using the class name as file name and find that file
        if (jClass == null) {
            // Get the name of the java file
            className = className + ".java";
            // Search for the java file in the project
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, className, GlobalSearchScope.allScope(proj));
            // If it couldn't be found, print an error message here for debugging purposes
            // If it isn't found, it does not necessarily mean there's a bug. It could be that a refactoring was
            // performed that wasn't handled yet
            if(pFiles.length == 0) {
                System.out.println("FAILED HERE");
                System.out.println(className);
                System.out.println(srcName);
                return;
            }
            // Get the first java file that was found
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            // Get the classes in that java file
            PsiClass[] jClasses = pFile.getClasses();
            for (PsiClass it : jClasses) {
                // Find the class that the refactoring happens in
                if (it.getQualifiedName().equals(qualifiedClass)) {
                    jClass = it;
                }
            }
        }
        // Get the methods in the class
        PsiMethod[] methods = jClass.getMethods();
        // Find the method being refactored
        for (PsiMethod method : methods) {
            if (method.getName().equals(destName)) {
                // Create a new rename processor using the original method name and the refactored method that we
                // found
                processor = new RenameProcessor(proj, method, srcName, false, false);
                RenameProcessor finalProcessor = processor;
                // Run the refactoring processor with the current modality
                ApplicationManager.getApplication().invokeAndWait(() -> finalProcessor.doRun(), ModalityState.current());
                // Update the virtual file that contains the refactoring
                VirtualFile vFile = jClass.getContainingFile().getVirtualFile();
                vFile.refresh(false, true);
                break;
            }
        }
    }

    /*
     * Undo the class refactoring that was originally performed.
     */
    public void undoRenameClass(Refactoring ref) {
        // Get the original class name
        String srcClass = ((RenameClassRefactoring) ref).getOriginalClassName();
        // Get the new class name
        String renamedClass = ((RenameClassRefactoring) ref).getRenamedClassName();
        // Trim the names to only get the class instead of the path
        String srcClassName = srcClass.substring(srcClass.lastIndexOf(".") + 1).trim();
        String renamedClassName = renamedClass.substring(renamedClass.lastIndexOf(".") + 1).trim();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        PsiClass jClass = jPF.findClass(renamedClass, GlobalSearchScope.allScope((proj)));
        // If the class isn't found, there might not have been a gradle file and we need to find the class another way
        if(jClass == null) {
            // Get the name of the file
            String qClass = renamedClassName + ".java";
            // Look for the file in the project
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, qClass, GlobalSearchScope.allScope(proj));
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            PsiClass[] jClasses = pFile.getClasses();
            // Find the class that's being refactored in that file
            for(PsiClass psiClass : jClasses) {
                if(psiClass.getQualifiedName().equals(renamedClass)) {
                    // Create a rename processor using the original name of the class and the psi class
                    RenameProcessor processor = new RenameProcessor(proj, psiClass, srcClassName, true, true);
                    RenameProcessor finalProcessor = processor;
                    Application app = ApplicationManager.getApplication();
                    // Run the rename class processor in the current modality state
                    app.invokeAndWait(() -> finalProcessor.run(), ModalityState.current());
                    // Update the virtual file of the class
                    VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
                    vFile.refresh(false, true);
                }

            }
        }
        // If we could find the class
        else {
            // Create a rename processor using the original name of the class and the psi class
            RenameProcessor proc = new RenameProcessor(proj, jClass, srcClassName, false, false);
            // Run the rename class processor
            proc.run();
            // Update the virtual file of the class
            VirtualFile vFile = jClass.getContainingFile().getVirtualFile();
            vFile.refresh(false, true);
        }
    }




}
