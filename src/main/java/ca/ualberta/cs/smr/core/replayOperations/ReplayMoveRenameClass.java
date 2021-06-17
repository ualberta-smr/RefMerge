package ca.ualberta.cs.smr.core.replayOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesProcessor;
import com.intellij.refactoring.move.moveClassesOrPackages.SingleSourceRootMoveDestination;
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
            return;
        }

        if(moveRenameClassObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiClass, destClassName, true, true);
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
        }
        if(moveRenameClassObject.isMoveMethod()) {
            // If the move move class refactoring involves an inner class, skip it for now
            if(moveRenameClassObject.isMoveInner() || moveRenameClassObject.isMoveOuter()) {
                return;
            }
            // use the destination package to undo the move class
            String destinationPackage = moveRenameClassObject.getDestinationClassObject().getPackageName();
            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
            assert psiPackage != null;
            PsiDirectory[] psiDirectories = psiPackage.getDirectories();
            PsiDirectory psiDirectory = psiDirectories[0];
            if(psiDirectories.length > 1) {
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
            if(originalDirectory.getFiles().length == 0) {
                if (!ApplicationManager.getApplication().isUnitTestMode()) {
                    originalDirectory.delete();
                }

            }
        }
        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }
}
