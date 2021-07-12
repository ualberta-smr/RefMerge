package ca.ualberta.cs.smr.core.undoOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.*;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassToInnerProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
import com.intellij.refactoring.move.moveInner.MoveInnerProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;

import java.util.Objects;

public class UndoMoveRenameClass {

    Project project;

    public UndoMoveRenameClass(Project project) {
        this.project = project;
    }

    /*
     * Undo the rename class refactoring that was originally performed by performing another rename class refactoring.
     */
    public void undoMoveRenameClass(RefactoringObject ref) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) ref;
        String srcQualifiedClass = moveRenameClassObject.getOriginalClassObject().getClassName();
        String destQualifiedClass = moveRenameClassObject.getDestinationClassObject().getClassName();
        String srcClassName = srcQualifiedClass.substring(srcQualifiedClass.lastIndexOf(".") + 1);
        String filePath = moveRenameClassObject.getDestinationFilePath();
        Utils utils = new Utils(project);
        utils.addSourceRoot(filePath);
        if(destQualifiedClass.contains("CompilationUnitTreeMatcher")) {
            System.out.println();
        }
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(destQualifiedClass, filePath);
        if(psiClass == null) {
            return;
        }
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        if(moveRenameClassObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiClass, srcClassName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        if(moveRenameClassObject.isMoveMethod()) {
            // If the move class refactoring is outer to inner
            if(moveRenameClassObject.isMoveInner()) {
                // If the outer to inner move class happens in the same file
                if(moveRenameClassObject.isSameFile()) {
                    vFile = psiClass.getContainingFile().getVirtualFile();
                    moveClassOuterInFile(psiClass, srcClassName, moveRenameClassObject.getStartOffset());
                }
                // Otherwise if the outer class is moved inner in a different file
                else {
                    String originalPackage = moveRenameClassObject.getOriginalClassObject().getPackageName();
                    PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
                    assert psiPackage != null;
                    PsiDirectory[] psiDirectories = psiPackage.getDirectories();
                    PsiDirectory targetContainer = psiDirectories[0];
                    if (psiDirectories.length > 1) {
                        String path = filePath.substring(0, filePath.lastIndexOf("/"));
                        for (PsiDirectory directory : psiDirectories) {
                            String dirPath = directory.getVirtualFile().getPath();
                            if (dirPath.contains(path)) {
                                targetContainer = directory;
                                break;
                            }
                        }
                    }
                    MoveInnerProcessor processor = new MoveInnerProcessor(project, null);
                    processor.setup(psiClass, srcClassName, true,
                            null, true, false, targetContainer);
                    ApplicationManager.getApplication().invokeAndWait(processor);
                }
            }
            // If the move class refactoring is inner to outer
            else if(moveRenameClassObject.isMoveOuter()) {
                // If the inner to outer move class happens in the same file
                if(moveRenameClassObject.isSameFile()) {
                    vFile = psiClass.getContainingFile().getVirtualFile();
                    moveClassInnerInFile(psiClass, srcClassName, moveRenameClassObject.getStartOffset());
                }
                // Otherwise if the inner to outer move class happens in different files
                else {
                    PsiClass[] psiClasses = new PsiClass[1];
                    psiClasses[0] = psiClass;
                    String originalTopClass = moveRenameClassObject.getOriginalClassObject().getPackageName();
                    PsiClass targetClass = utils.getPsiClassByFilePath(filePath, originalTopClass);
                    if(targetClass == null) {
                        return;
                    }
                    MoveClassToInnerProcessor processor = new MoveClassToInnerProcessor(project, psiClasses, targetClass,
                            true, false, null);
                    ApplicationManager.getApplication().invokeAndWait(processor, ModalityState.current());
                }
            }
            // Move the inner class to another class
            else if(moveRenameClassObject.isMoveInnerToInner()) {
                filePath = moveRenameClassObject.getOriginalFilePath();
                String originalPackage = moveRenameClassObject.getOriginalClassObject().getPackageName();
                //String containingClass = originalPackage.substring(0, originalPackage.lastIndexOf("."));
                PsiClass targetContainer = utils.getPsiClassByFilePath(filePath, originalPackage);
                if(targetContainer == null) {
                    return;
                }
                MoveInnerProcessor processor = new MoveInnerProcessor(project, null);
                processor.setup(psiClass, srcClassName, false,
                        null, true, false, targetContainer);
                try {
                    ApplicationManager.getApplication().invokeAndWait(processor, ModalityState.current());
                }
                catch(NullPointerException e) {
                    System.out.println(destQualifiedClass);
                    e.printStackTrace();
                    return;
                }
            }
            // Otherwise if the move class moves a top level class from one package to another
            else {
                // use the original package to undo the move class
                String originalPackage = moveRenameClassObject.getOriginalClassObject().getPackageName();
                PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
                if (psiPackage == null) {
                    MoveDestination moveDestination = JavaRefactoringFactory
                            .getInstance(project).createSourceFolderPreservingMoveDestination(originalPackage);
                    PsiElement[] psiElements = new PsiElement[1];
                    psiElements[0] = psiClass;
                    MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                            moveDestination, true, false, null);
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(moveClassProcessor);
                } else {
                    PsiDirectory dir = psiPackage.getDirectories()[0];
                    PsiElement[] psiElements = new PsiElement[1];
                    psiElements[0] = psiClass;
                    MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                            new SingleSourceRootMoveDestination(PackageWrapper
                                    .create(Objects.requireNonNull(JavaDirectoryService.getInstance().getPackage(dir))), dir),
                            true, false, null);
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(moveClassProcessor, ModalityState.current());

                }
            }
        }

        UsageViewManager viewManager = UsageViewManager.getInstance(project);
        UsageView usageView = viewManager.getSelectedUsageView();
        if(usageView != null) {
            usageView.close();
        }

        // Update the virtual file of the class
        vFile.refresh(false, true);

    }

    /*
     * Move the inner class out of the class in the same file.
     */
    private void moveClassOuterInFile(PsiClass psiClass, String originalClassName, int startOffset) {
        PsiFile psiFile = psiClass.getContainingFile();
        PsiClass outerClass = psiClass.getContainingClass();
        PsiClass[] psiClasses = ((PsiJavaFile) psiFile).getClasses();
        for(PsiClass candidateClass : psiClasses) {
           int candidateOffset = candidateClass.getTextOffset();
           if(candidateOffset > startOffset) {
               break;
           }
           outerClass = candidateClass;
        }
        final PsiClass newClass = PsiElementFactory.getInstance(project).createClassFromText(psiClass.getText(), psiFile);
        PsiClass finalOuterClass = outerClass;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiClass finalClass = (PsiClass) psiFile.addAfter(newClass, finalOuterClass);
            finalClass.setName(originalClassName);
            psiClass.delete();
        });


    }

    /*
     * Move the outer class in the class into a class in the same file.
     */
    private void moveClassInnerInFile(PsiClass psiClass, String originalClassName, int startOffset) {
        PsiFile psiFile = psiClass.getContainingFile();
        PsiClass[] psiClasses = ((PsiJavaFile) psiFile).getClasses();
        PsiClass outerClass = psiClasses[0];
        psiClasses = outerClass.getInnerClasses();
        PsiClass previousClass = null;
        for(PsiClass candidateClass : psiClasses) {
            int candidateOffset = candidateClass.getTextOffset();
            if(candidateOffset > startOffset) {
                break;
            }
            previousClass = candidateClass;
        }
        final PsiClass newClass = PsiElementFactory.getInstance(project).createClassFromText(psiClass.getText(), psiFile);
        PsiClass finalPreviousClass = previousClass;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiClass finalClass = (PsiClass) outerClass.addAfter(newClass, finalPreviousClass);
            finalClass.setName(originalClassName);
            psiClass.delete();
        });

    }


}
