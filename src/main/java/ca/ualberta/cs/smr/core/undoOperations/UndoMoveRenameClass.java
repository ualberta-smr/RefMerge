package ca.ualberta.cs.smr.core.undoOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.*;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
import com.intellij.usageView.UsageInfo;

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
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(destQualifiedClass, filePath);
        assert psiClass != null;

        if(moveRenameClassObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiClass, srcClassName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        if(moveRenameClassObject.isMoveMethod()) {
            // If the move move class refactoring involves an inner class, skip it for now
            if(moveRenameClassObject.isMoveInner() || moveRenameClassObject.isMoveOuter()) {
                return;
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
                            moveDestination, true, true, null);
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(moveClassProcessor);
                } else {
                    PsiDirectory dir = psiPackage.getDirectories()[0];
                    PsiElement[] psiElements = new PsiElement[1];
                    psiElements[0] = psiClass;
                    MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                            new SingleSourceRootMoveDestination(PackageWrapper
                                    .create(Objects.requireNonNull(JavaDirectoryService.getInstance().getPackage(dir))), dir),
                            true, true, null);
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(moveClassProcessor);
                }
            }
        }
        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

}
