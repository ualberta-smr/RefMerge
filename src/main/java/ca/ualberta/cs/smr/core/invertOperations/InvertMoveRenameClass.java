package ca.ualberta.cs.smr.core.invertOperations;

import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbService;
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
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class InvertMoveRenameClass {

    Project project;

    public InvertMoveRenameClass(Project project) {
        this.project = project;
    }

    /*
     * Invert the rename class refactoring that was originally performed by performing another rename class refactoring.
     */
    public void invertMoveRenameClass(RefactoringObject ref) {
        MoveRenameClassObject moveRenameClassObject = (MoveRenameClassObject) ref;
        String srcQualifiedClass = moveRenameClassObject.getOriginalClassObject().getClassName();
        String destQualifiedClass = moveRenameClassObject.getDestinationClassObject().getClassName();
        String srcClassName = srcQualifiedClass.substring(srcQualifiedClass.lastIndexOf(".") + 1);
        String filePath = moveRenameClassObject.getDestinationFilePath();
        Utils utils = new Utils(project);

        utils.addSourceRoot(filePath, destQualifiedClass);

        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(destQualifiedClass, filePath);
        if(psiClass == null) {
            return;
        }
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        if(moveRenameClassObject.isRenameMethod()) {
            RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
            RenameRefactoring renameRefactoring = factory.createRename(psiClass, srcClassName, true, true);
            renameRefactoring.respectAllAutomaticRenames();
            renameRefactoring.respectEnabledAutomaticRenames();
            UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
            renameRefactoring.doRefactoring(refactoringUsages);
            DumbService.getInstance(project).completeJustSubmittedTasks();
        }
        if(moveRenameClassObject.isMoveMethod()) {
            // If the move class refactoring is outer to inner
            if(moveRenameClassObject.isMoveInner()) {
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
            // If the move class refactoring is inner to outer
            else if(moveRenameClassObject.isMoveOuter()) {

                PsiClass[] psiClasses = new PsiClass[1];
                psiClasses[0] = psiClass;
                String originalTopClass = moveRenameClassObject.getOriginalClassObject().getPackageName();
                filePath = moveRenameClassObject.getOriginalFilePath();
                PsiClass targetClass = utils.getPsiClassByFilePath(filePath, originalTopClass);
                if(targetClass == null) {
                    return;
                }
                MoveClassToInnerProcessor processor = new MoveClassToInnerProcessor(project, psiClasses, targetClass,
                        true, false, null);
                ApplicationManager.getApplication().invokeAndWait(processor, ModalityState.current());
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
                PsiDirectory psiDirectory = null;
                if(psiPackage != null) {
                    filePath = moveRenameClassObject.getOriginalFilePath();
                    if(filePath.contains("/")) {
                        filePath = filePath.substring(0, filePath.lastIndexOf("/"));
                    }
                    PsiDirectory[] directories = psiPackage.getDirectories();
                    for(PsiDirectory directory : directories) {
                        String path = directory.getVirtualFile().getPath();
                        if(path.contains(filePath)) {
                            psiDirectory = directory;
                            break;
                        }
                    }

                }
                if (psiDirectory == null) {
                    // Create the target directory
                    psiDirectory = psiClass.getContainingFile().getContainingDirectory();
                    // Get the common package
                    String commonPackage = getCommonPackage(psiDirectory, originalPackage);
                    // If RefMiner does not report the full package
                    if(commonPackage == null) {
                        return;
                    }
                    PsiDirectory parentDirectory = getParentDirectory(psiDirectory, commonPackage);
                    String relativePackage = originalPackage.substring(commonPackage.length() + 1);
                    PsiDirectory subDirectory = createSubdirectory(parentDirectory, relativePackage);

                    MoveDestination moveDestination = new SingleSourceRootMoveDestination(PackageWrapper
                            .create(Objects.requireNonNull(JavaDirectoryService.getInstance().getPackage(subDirectory))), subDirectory);


                    PsiElement[] psiElements = new PsiElement[1];
                    psiElements[0] = psiClass;
                    MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                            moveDestination, true, false, null);
                    Application app = ApplicationManager.getApplication();
                    app.invokeAndWait(moveClassProcessor);
                } else {
                    PsiElement[] psiElements = new PsiElement[1];
                    psiElements[0] = psiClass;
                    MoveClassesOrPackagesProcessor moveClassProcessor = new MoveClassesOrPackagesProcessor(project, psiElements,
                            new SingleSourceRootMoveDestination(PackageWrapper
                                    .create(Objects.requireNonNull(JavaDirectoryService.getInstance().getPackage(psiDirectory))), psiDirectory),
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
     * Get the common package of the directory of the refactored class and the package of the original class
     */
    private String getCommonPackage(PsiDirectory psiDirectory, String originalPackage) {
        String directoryPackage = psiDirectory.getPresentation().getLocationString();
        String[] packages = new String[2];
        packages[0] = originalPackage;
        packages[1] = directoryPackage;
        String commonPackage = StringUtils.getCommonPrefix(packages);
        if(commonPackage.length() == 0) {
            return null;
        }
        if(commonPackage.contains(".")) {
            commonPackage = commonPackage.substring(0, commonPackage.lastIndexOf("."));
        }
        return commonPackage;
    }

    /*
     * Get the PSI directory of the common package
     */
    private PsiDirectory getParentDirectory(PsiDirectory psiDirectory, String commonPackage) {
        if(psiDirectory.getPresentation().getLocationString().equals(commonPackage)) {
           return psiDirectory;
        }
        return getParentDirectory(psiDirectory.getParentDirectory(), commonPackage);
    }

    /*
     * Create the specified subdirectory
     */
    private PsiDirectory createSubdirectory(PsiDirectory psiDirectory, String relativePath) {
        if(!relativePath.contains(".")) {
            PsiDirectory candidate = psiDirectory.findSubdirectory(relativePath);
            if(candidate != null) {
                return candidate;
            }
            else {
                AtomicReference<PsiDirectory> subDirectory = new AtomicReference<>();
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    subDirectory.set(psiDirectory.createSubdirectory(relativePath));
                });
                return subDirectory.get();
            }
        }
        String[] directories = relativePath.split("\\.");
        String newSubDirectory = directories[0];
        PsiDirectory candidate = psiDirectory.findSubdirectory(newSubDirectory);
        if(candidate != null) {
            return createSubdirectory(candidate, relativePath.substring(newSubDirectory.length() + 1));
        }
        else {
            AtomicReference<PsiDirectory> subDirectory = new AtomicReference<>();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                subDirectory.set(psiDirectory.createSubdirectory(newSubDirectory));
            });

            return createSubdirectory(subDirectory.get(), relativePath.substring(newSubDirectory.length() + 1));
        }
    }

}
