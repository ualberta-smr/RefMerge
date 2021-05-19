package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Utils {
    Project project;

    public Utils(Project project) {
        this.project = project;
    }

    /*
     * Runs a command such as "cp -r ..." or "git merge-files ..."
     */
    public static void runSystemCommand(String... commands) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Save the content of one directory to another
     */
    public static void saveContent(Project project, String dir) {
        // Save project to temporary directory
        String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("cp", "-r", project.getBasePath(), path);
    }

    /*
     * Remove the temp files
     */
    public static void clearTemp() {
        String path = System.getProperty("user.home") + "/temp/right";
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("rm", "-rf", path);
        path = System.getProperty("user.home") + "/temp/left";
        runSystemCommand("rm", "-rf", path);
        path = System.getProperty("user.home") + "/temp/base";
        runSystemCommand("rm", "-rf", path);
    }

    public static void dumbServiceHandler(Project project) {
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            // Waits for the task to finish
            dumbService.completeJustSubmittedTasks();
        }
    }

    public static void refreshVFS() {
        VirtualFileManager vFM = VirtualFileManager.getInstance();
        vFM.refreshWithoutFileWatcher(false);
    }

    public static void reparsePsiFiles(Project project) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();
    }

    public static boolean ifSameMethods(PsiMethod method, UMLOperation operation) {
        PsiParameter[] psiParameterList = method.getParameterList().getParameters();
        List<UMLParameter> umlParameters = operation.getParameters();
        String umlName = operation.getName();
        String psiName = method.getName();
        int firstUMLParam = 0;
        // Check if the method names are the same
        if (!umlName.equals(psiName)) {
            return false;
        }
        // If the number of parameters are different, the methods are different
        // Subtract 1 from umlParameters because umlParameters includes return type
        if (!operation.isConstructor()) {
            if (umlParameters.size() - 1 != psiParameterList.length) {
                return false;
            }
            PsiType psiReturnType = method.getReturnType();
            assert psiReturnType != null;
            String psiType = psiReturnType.getPresentableText();
            UMLParameter umlParameter = umlParameters.get(0);
            String umlType = umlParameter.getType().toString();
            // Check if the return types are the same
            if (!psiType.equals(umlType)) {
                // Check if UML type is class type
                if (umlType.contains(".")) {
                    umlType = umlType.substring(umlType.lastIndexOf(".") + 1);
                    if (!umlType.equals(psiType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            firstUMLParam = 1;
        }
        else {
            if(umlParameters.size() != psiParameterList.length) {
                return false;
            }
        }
        // Check if the parameters are the same
        return parameterComparator(firstUMLParam, umlParameters, psiParameterList);
    }

    private static boolean parameterComparator(int firstUMLParam, List<UMLParameter> umlParameters,
                                        PsiParameter[] psiParameterList) {
        UMLParameter umlParameter;
        String umlType;
        String psiType;
        // Check if the parameters are the same
        for(int i = firstUMLParam; i < umlParameters.size(); i++) {
            int j = i;
            if(firstUMLParam == 1) {
                j = i - 1;
            }
            umlParameter = umlParameters.get(i);
            PsiParameter psiParameter = psiParameterList[j];
            umlType = umlParameter.getType().toString();
            String parameterName = psiParameter.getName();
            psiType = psiParameter.getText();
            psiType = psiType.substring(0, psiType.lastIndexOf(parameterName) - 1);
            if(!umlType.equals(psiType)) {
                return false;
            }

        }
        return true;
    }

    public PsiClass getPsiClassByFilePath(String filePath, String qualifiedClass) {
        // Get the name of the java file without the path
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        // If no files are found, give an error message for debugging
        if(psiFiles.length == 0) {
            System.out.println("FAILED HERE");
            System.out.println(filePath);
            return null;
        }
        for (PsiFile file : psiFiles) {
            PsiJavaFile psiFile = (PsiJavaFile) file;
            // Get the classes in the file
            PsiClass[] jClasses = psiFile.getClasses();
            for (PsiClass it : jClasses) {
                // Find the class that the refactoring happens in
                if (Objects.equals(it.getQualifiedName(), qualifiedClass)) {
                    return it;
                }
                if(qualifiedClass.contains(it.getName())) {
                    return it;
                }
                PsiClass[] innerClasses = it.getInnerClasses();
                for (PsiClass innerIt : innerClasses) {
                    if (Objects.equals(innerIt.getQualifiedName(), qualifiedClass)) {
                        return innerIt;
                    }
                }
            }
        }
        return null;
    }

    public PsiClass getPsiClassFromClassAndFileNames(String className, String filePath) {
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(project);
        PsiClass psiClass = jPF.findClass(className, GlobalSearchScope.allScope((project)));
        // If the class isn't found, there might not have been a gradle file and we need to find the class another way
        if(psiClass == null) {
            psiClass = getPsiClassByFilePath(filePath, className);
        }
        return psiClass;
    }

    public static PsiMethod getPsiMethod(PsiClass psiClass, UMLOperation operation) {
        PsiMethod[] methods = psiClass.getMethods();
        for(PsiMethod method : methods) {
            if(Utils.ifSameMethods(method, operation)) {
                return method;
            }
        }
        return null;
    }

    public static PsiJavaCodeReferenceElement getPsiReferenceExpressionsForExtractMethod(PsiMethod psiMethod, Project project) {
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        // Create renameRefactoring to find usages of the extracted method
        RenameRefactoring renameRefactoring = factory.createRename(psiMethod, "method", false, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        for(UsageInfo usageInfo : refactoringUsages) {
            PsiElement element = usageInfo.getElement();
            if(usageInfo.getElement() instanceof PsiReferenceExpression) {
                if(usageInfo.getElement() instanceof PsiJavaCodeReferenceElement) {
                    return (PsiJavaCodeReferenceElement) element;
                }
            }
        }
        return null;
    }
}

