package ca.ualberta.cs.smr.core.replayOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.*;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassToInnerProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
import com.intellij.refactoring.move.moveInner.MoveInnerProcessor;
import com.intellij.usageView.UsageInfo;

import java.util.Objects;

public class ReplayMoveRenameClass {

    Project project;

    public ReplayMoveRenameClass(Project project) {
        this.project = project;
    }

    public void replayMoveRenameClass(RefactoringObject ref) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) ref;
        String srcQualifiedClass = moveRenameClassObject.getOriginalClassObject().getClassName();
        String destQualifiedClass = moveRenameClassObject.getDestinationClassObject().getClassName();
        String destClassName = destQualifiedClass.substring(destQualifiedClass.lastIndexOf(".") + 1);
        Utils utils = new Utils(project);
        String filePath = moveRenameClassObject.getOriginalFilePath();
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(srcQualifiedClass, filePath);

        if(psiClass == null) {
            System.out.println("Class Refactoring Failed");
            return;
        }
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        if(moveRenameClassObject.isRenameMethod()) {

            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiClass, destClassName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        if(moveRenameClassObject.isMoveMethod()) {
            // If the move class refactoring is outer to inner
            if(moveRenameClassObject.isMoveInner()) {
                // If the outer to inner move class happens in the same file
                if(moveRenameClassObject.isSameFile()) {
                    vFile = psiClass.getContainingFile().getVirtualFile();
                    // Move the class back into the file
                    moveClassInnerInFile(psiClass, destClassName);
                }
                else {
                    PsiClass[] psiClasses = new PsiClass[1];
                    psiClasses[0] = psiClass;
                    String destinationPackage = moveRenameClassObject.getDestinationClassObject().getPackageName();
                    String destinationTopClass = destinationPackage.substring(destinationPackage.lastIndexOf(".") + 1);
                    filePath = moveRenameClassObject.getDestinationFilePath();
                    PsiClass targetClass = utils.getPsiClassByFilePath(filePath, destinationTopClass);
                    MoveClassToInnerProcessor processor = new MoveClassToInnerProcessor(project, psiClasses, targetClass,
                            true, false, null);
                    ApplicationManager.getApplication().invokeAndWait(processor);
                }
            }
            // If the move class refactoring is inner to outer
            else if(moveRenameClassObject.isMoveOuter()) {
                // If the inner to outer move class happens in the same file
                if(moveRenameClassObject.isSameFile()) {
                    vFile = psiClass.getContainingFile().getVirtualFile();
                    moveClassOuterInFile(psiClass, destClassName);
                }
                // Otherwise move the inner class to a new file
                else {
                    String destinationPackage = moveRenameClassObject.getDestinationClassObject().getPackageName();
                    PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
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
                    processor.setup(psiClass, destClassName, true,
                            null, true, false, targetContainer);
                    ApplicationManager.getApplication().invokeAndWait(processor);
                }
            }
            // Move the inner class to another class
            else if(moveRenameClassObject.isMoveInnerToInner()) {
                filePath = moveRenameClassObject.getDestinationFilePath();
                String containingClass = moveRenameClassObject.getDestinationClassObject().getPackageName();
                PsiClass targetContainer = utils.getPsiClassByFilePath(filePath, containingClass);
                if(psiClass.getContainingClass() == null) {
                    PsiClass[] psiClasses = new PsiClass[1];
                    psiClasses[0] = psiClass;
                    MoveClassToInnerProcessor processor = new MoveClassToInnerProcessor(project, psiClasses, targetContainer,
                            true, false, null);
                    ApplicationManager.getApplication().invokeAndWait(processor);
                }
                else {
                    MoveInnerProcessor processor = new MoveInnerProcessor(project, null);
                    processor.setup(psiClass, destClassName, false,
                            null, true, false, targetContainer);
                    ApplicationManager.getApplication().invokeAndWait(processor);
                }
            }
            // use the destination package to undo the move class if the class is outer to outer
            else {
                String destinationPackage = moveRenameClassObject.getDestinationClassObject().getPackageName();
                PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);

                if(psiPackage == null) {
                    return;
                }
                PsiDirectory[] psiDirectories = psiPackage.getDirectories();
                PsiDirectory psiDirectory = psiDirectories[0];
                if (psiDirectories.length > 1) {
                    filePath = moveRenameClassObject.getDestinationFilePath();
                    String path = filePath.substring(0, filePath.lastIndexOf("/"));
                    for (PsiDirectory directory : psiDirectories) {
                        String dirPath = directory.getVirtualFile().getPath();
                        if (dirPath.contains(path)) {
                            psiDirectory = directory;
                            break;
                        }
                    }
                }
                PsiElement[] psiElements = new PsiElement[1];
                // Get the original directory before moving the class
                PsiDirectory originalDirectory = psiClass.getContainingFile().getContainingDirectory();
                psiElements[0] = psiClass;
                MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                        new SingleSourceRootMoveDestination(PackageWrapper
                                .create(Objects.requireNonNull(JavaDirectoryService.getInstance()
                                        .getPackage(psiDirectory))), psiDirectory),
                        true, true, null);

                Application app = ApplicationManager.getApplication();
                app.invokeAndWait(moveClassProcessor);

                // If the original directory is empty after moving the class, delete the directory
                if (originalDirectory.getFiles().length == 0 && originalDirectory.getSubdirectories().length == 0) {
                    if (!ApplicationManager.getApplication().isUnitTestMode()) {
                        WriteCommandAction.runWriteCommandAction(project, originalDirectory::delete);
                    }

                }
            }
        }
        // Update the virtual file of the class
        vFile.refresh(false, true);

    }

    /*
     * Move the inner class out of the class in the same file.
     */
    private void moveClassOuterInFile(PsiClass psiClass, String destinationClassName) {
        PsiFile psiFile = psiClass.getContainingFile();
        PsiClass outerClass = psiClass.getContainingClass();
        final PsiClass newClass = PsiElementFactory.getInstance(project).createClassFromText(psiClass.getText(), psiFile);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiClass finalClass =  (PsiClass) psiFile.addAfter(newClass, outerClass);
            finalClass.setName(destinationClassName);
            psiClass.delete();
        });

    }

    /*
     * Move the outer class in the class into the public class in the same file.
     */
    private void moveClassInnerInFile(PsiClass psiClass, String destinationClassName) {
        PsiFile psiFile = psiClass.getContainingFile();
        PsiClass[] psiClasses = PsiTreeUtil.getChildrenOfType(psiFile, PsiClass.class);
        PsiClass outerClass = psiClasses[0];
        final PsiClass newClass = PsiElementFactory.getInstance(project).createClassFromText(psiClass.getText(), psiFile);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiClass finalClass = (PsiClass) outerClass.addAfter(newClass, null);
            finalClass.setName(destinationClassName);
            psiClass.delete();
        });

    }
}
