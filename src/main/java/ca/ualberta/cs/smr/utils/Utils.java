package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.usageView.UsageInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {
    Project project;

    public static final String CONFLICT_LEFT_BEGIN = "<<<<<<<";
    public static final String CONFLICT_RIGHT_END = ">>>>>>>";

    private static final boolean LOG_TO_FILE  = true;
    private static final String LOG_FILE = "log.txt";


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
//            p.waitFor(200, TimeUnit.SECONDS);
//            p.destroy();
            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void log(String projectName, Object message) {
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z").format(new Date());
        String logMessage = timeStamp + " ";
        if (message instanceof String){
            logMessage += (String) message;
        } else if (message instanceof Exception) {
            logMessage += ((Exception) message).getMessage() + "\n";
            StringBuilder stackBuilder = new StringBuilder();
            StackTraceElement[] stackTraceElements = ((Exception) message).getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                stackBuilder.append(stackTraceElement.toString());
                if (i < stackTraceElements.length - 1) stackBuilder.append("\n");
            }
            logMessage += stackBuilder.toString();
        } else {
            logMessage = message.toString();
        }
        System.out.println(logMessage);

        if (LOG_TO_FILE) {
            String logPath = LOG_FILE;
            if (projectName != null && !projectName.trim().equals("")) logPath = projectName;
            try {
                String path = System.getProperty("user.home") + "/temp/logs/";
                new File(path).mkdirs();
                Files.write(Paths.get(path + logPath), Arrays.asList(logMessage),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeContent(String path, String content) {
        try {
            Files.write(Paths.get(path), Arrays.asList(content),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * Save the content of one directory to another. Return the path
     */
    public static String saveContent(Project project, String path) {
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("cp", "-r", project.getBasePath() + "/.", path);
        return path;
    }

    /*
     * Remove the temp files
     */
    public static void clearTemp(String dir) {
        //String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(dir);
        file.mkdirs();
        runSystemCommand("rm", "-rf", dir);
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

    /*
     * Use the file path to add the source root to the module if it is not already in the module.
     */
    public void addSourceRoot(String filePath, String filePackage) {
        // There are no modules or source roots in unit test mode
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }
        boolean isTestFolder = filePath.contains("test");

        String projectPath = project.getBasePath();
        String relativePath = projectPath + "/" + filePath;
        //relativePath = getRelativePathOfSourceRoot(relativePath, project.getName());
        filePackage = filePackage.replaceAll("\\.", "/");
        String path = "";
        try {
            path = relativePath.substring(0, relativePath.indexOf(filePackage));
        }
        catch(StringIndexOutOfBoundsException e) {
            path = getRelativePathOfSourceRoot(relativePath, project.getName());
        }
        File directory = new File(path);
        VirtualFile sourceVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
        if(sourceVirtualFile == null) {
            return;
        }
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        // Get the first module that does not depend on any other modules
        ArrayList<Module> modules = getModule(sourceVirtualFile, moduleManager.getModules());
        if(modules == null) {
            return;
        }
        for(Module module : modules) {
            AtomicReference<ModifiableRootModel> rootModel = new AtomicReference<>();
            ReadAction.run(() -> {
                rootModel.set(ModuleRootManager.getInstance(module).getModifiableModel());
            });
            directory = new File(Objects.requireNonNull(PathMacroUtil.getModuleDir(module.getModuleFilePath())));
            VirtualFile moduleVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
            ContentEntry contentEntry = getContentEntry(moduleVirtualFile, rootModel.get());
            if(contentEntry == null) {
                continue;
            }
            if (checkIfSourceFolderExists(sourceVirtualFile, contentEntry)) {
                WriteAction.run(rootModel.get()::dispose);
                return;
            }
            else {
                contentEntry.addSourceFolder(sourceVirtualFile, isTestFolder);
                WriteAction.run(rootModel.get()::commit);
                Utils.dumbServiceHandler(project);
                break;
            }
        }
    }

    /*
     * Get the relative path of the source root folder.
     */
    private String getRelativePathOfSourceRoot(String relativePath, String projectName) {
        // If the relative path contains java, then that's the source folder.
        if(relativePath.contains("java/")) {
            return relativePath.substring(0, relativePath.lastIndexOf("java/") + 4);
        }
        if(relativePath.contains("resources/")) {
            return relativePath.substring(0, relativePath.lastIndexOf("resources/") + 9);
        }
        // Get the project name
        String temp = relativePath.substring(relativePath.indexOf(projectName) + projectName.length());
        // If the relative path contains the project name a second time, use that as a source folder.
        if(temp.contains(projectName)) {
            return relativePath.substring(0, relativePath.lastIndexOf(projectName));
        }
        // Otherwise return the src directory
        else {
            return relativePath.substring(0, relativePath.indexOf("src/") + 3);
        }

    }
    /*
     * Get the module that the virtual file is in.
     */
    private ArrayList<Module> getModule(VirtualFile virtualFile, Module[] modules) {
        ArrayList<Module> potentialModules = new ArrayList<>();
        for(Module module : modules) {
            VirtualFile moduleFile = module.getModuleFile();
            if(moduleFile == null) {
                continue;
            }
            VirtualFile moduleFileParent = moduleFile.getParent();
            // Get the src directory
            VirtualFile virtualFileParent = virtualFile.getParent();
            // If the src directory and .iml file are in the same module
            if(moduleFileParent.equals(virtualFileParent.getParent())) {
                potentialModules.add(module);
            }
        }
        // If we could not find the module, there's probably only one module file in ./idea
        if(modules.length > 0 && potentialModules.size() == 0) {
            potentialModules.add(modules[0]);
            return potentialModules;
        }
        return potentialModules;
    }

    /*
     * Get the content entry in the specified module.
     */
    private ContentEntry getContentEntry(VirtualFile moduleVirtualFile, ModifiableRootModel rootModel) {
        for(ContentEntry contentEntry : rootModel.getContentEntries()) {
            if(contentEntry.getFile().equals(moduleVirtualFile)) {
                return contentEntry;
            }
        }
        return null;
    }

    /*
     * Check if the virtual file already exists as a source folder to avoid unnecessary indexing.
     */
    private boolean checkIfSourceFolderExists(VirtualFile sourceVirtualFile, ContentEntry contentEntry) {
        VirtualFile[] sourceFolderFiles = contentEntry.getSourceFolderFiles();
        for(VirtualFile sourceFolderFile : sourceFolderFiles) {
            if(sourceVirtualFile.equals(sourceFolderFile)) {
                return true;
            }
        }
        return false;
    }

    public static void reparsePsiFiles(Project project) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();
    }

    public static boolean ifSameMethods(PsiMethod method, MethodSignatureObject methodSignature) {
        PsiParameter[] psiParameterList = method.getParameterList().getParameters();
        List<ParameterObject> parameters = methodSignature.getParameterList();
        String umlName = methodSignature.getName();
        String psiName = method.getName();
        int firstUMLParam = 0;
        // Check if the method names are the same
        if (!umlName.equals(psiName)) {
            return false;
        }
        // If the number of parameters are different, the methods are different
        // Subtract 1 from umlParameters because umlParameters includes return type
        if (!methodSignature.isConstructor()) {
            if (parameters.size() - 1 != psiParameterList.length) {
                return false;
            }
            PsiType psiReturnType = method.getReturnType();
            assert psiReturnType != null;
            String psiType = psiReturnType.getPresentableText();
            ParameterObject parameterObject = parameters.get(0);
            String parameterType = parameterObject.getType();
            // Check if the return types are the same
            if (!psiType.equals(parameterType)) {
                // Check if UML type is class type
                if (parameterType.contains(".")) {
                    parameterType = parameterType.substring(parameterType.lastIndexOf(".") + 1);
                    if (!parameterType.equals(psiType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            firstUMLParam = 1;
        }
        else {
            if(parameters.size() != psiParameterList.length) {
                return false;
            }
        }
        // Check if the parameters are the same
        return parameterComparator(firstUMLParam, parameters, psiParameterList);
    }

    /*
     * Compare the parameters in the UML parameter list to the parameters in the PSI parameter list to see if
     * the method signatures are the same.
     */
    private static boolean parameterComparator(int firstUMLParam, List<ParameterObject> parameters,
                                        PsiParameter[] psiParameterList) {
        ParameterObject parameterObject;
        String umlType;
        String psiType;
        // Check if the parameters are the same
        for(int i = firstUMLParam; i < parameters.size(); i++) {
            int j = i - firstUMLParam;
            parameterObject = parameters.get(i);
            PsiParameter psiParameter = psiParameterList[j];
            umlType = parameterObject.getType();

            String parameterName = psiParameter.getName();
            psiType = psiParameter.getText();
            psiType = psiType.substring(0, psiType.lastIndexOf(parameterName) - 1);
            // If the parameter has the final modifier, remove it for comparison with UML parameter.
            if(psiParameter.hasModifierProperty(PsiModifier.FINAL)) {
                psiType = psiType.substring(psiType.indexOf("final ") + 6);
            }
            // Replace int... with int[] for comparison with RefMiner object
            if(psiType.contains("...")) {
                psiType = psiType.replace("...", "[]");
            }
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
            String classPath = file.getVirtualFile().getPath();
            if(!classPath.contains(filePath)) {
                continue;
            }
            PsiJavaFile psiFile = (PsiJavaFile) file;
            // Get the classes in the file
            PsiClass[] jClasses = psiFile.getClasses();
            for (PsiClass it : jClasses) {
                // Find the class that the refactoring happens in
                if (Objects.equals(it.getQualifiedName(), qualifiedClass)) {
                    return it;
                }
                // Need to update tests to remove this
                if (ApplicationManager.getApplication().isUnitTestMode()) {
                    if(qualifiedClass.contains(it.getName())) {
                        return it;
                    }
                }
                PsiClass[] innerClasses = it.getInnerClasses();
                for (PsiClass innerIt : innerClasses) {
                    if (Objects.equals(innerIt.getQualifiedName(), qualifiedClass)) {
                        return innerIt;
                    }
                }
            }
            for(PsiClass it : jClasses) {
                String qName = it.getQualifiedName();
                qName = qName.substring(qName.lastIndexOf(".") + 1);
                String otherName = qualifiedClass.substring(qualifiedClass.lastIndexOf(".") + 1);
                if(Objects.equals(qName, otherName)) {
                    return it;
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

    public static PsiMethod getPsiMethod(PsiClass psiClass, MethodSignatureObject methodSignatureObject) {
        PsiMethod[] methods = psiClass.getMethods();
        for(PsiMethod method : methods) {
            if(Utils.ifSameMethods(method, methodSignatureObject)) {
                return method;
            }
        }
        return null;
    }

    /*
     * Format the text to remove new lines and spaces for comparing code fragments
     */
    public static String formatText(String text) {
        text = text.replaceAll(" ", "");
        text = text.replaceAll("\n", "");
        return text;
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

    public void removeRefactoringsInConflictingFile(String path, List<RefactoringObject> refactorings) {
        if(!path.endsWith(".java")) {
            return;
        }
        List<Pair<Integer, Integer>> conflictingRegions = getConflictingRegions(path);
        for(RefactoringObject refactoring : refactorings) {
            if(refactoring instanceof InlineMethodObject || refactoring instanceof ExtractMethodObject) {
                continue;
            }

            if (!refactoring.getOriginalFilePath().equals(path)) {
                continue;
            }
            setBoundaries(refactoring);
            if (!checkReplayRefactoring(refactoring, conflictingRegions)) {
                refactorings.remove(refactoring);
            }
        }
    }

    private List<Pair<Integer, Integer>> getConflictingRegions(String path) {
        File file = new File(path);
        List<Pair<Integer, Integer>> conflictingRegions = new ArrayList<>();
        int startLine = 0;
        int endLine = 0;
        int counter = 0;
        try {
            InputStream stream = file.toURI().toURL().openStream();

        for(String line : getLinesFromInputStream(stream)) {
            counter++;
            if(line.contains(CONFLICT_LEFT_BEGIN)) {
                startLine = counter;
            }
            else if(line.contains(CONFLICT_RIGHT_END)) {
                endLine = counter;
                conflictingRegions.add(Pair.of(startLine, endLine));
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conflictingRegions;
    }

    /*
     * Get each line from the input stream containing the IntelliMerge dataset.
     */
    public static ArrayList<String> getLinesFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }

    private boolean checkReplayRefactoring(RefactoringObject refactoring, List<Pair<Integer, Integer>> conflictingRegions) {
        int refStartLine = refactoring.getStartLine();
        int refEndLine = refactoring.getEndLine();
        for(Pair<Integer,Integer> conflictingRegion : conflictingRegions) {
            int conflictingStartLine = conflictingRegion.getLeft();
            int conflictingEndLine = conflictingRegion.getRight();
            // If the refactoring is within the conflicting region, do not replay
            if(refStartLine > conflictingStartLine && refEndLine < conflictingEndLine) {
                return false;
            }
            // Otherwise, if the regions overlap, do not replay
            else if(refStartLine < conflictingStartLine && refEndLine < conflictingEndLine) {
                return false;
            }
            else if(refStartLine > conflictingStartLine && refEndLine > conflictingEndLine) {
                return false;
            }
        }
        // If the regions are not related or the region is within the method or class, replay
        return true;
    }

    /*
     * Get the boundaries for the given PSI class
     */
    private void setBoundaries(RefactoringObject ref) {
        PsiElement psiElement;
        if(ref instanceof MoveRenameMethodObject) {
            String filePath = ref.getOriginalFilePath();
            String className = ((MoveRenameMethodObject) ref).getOriginalClassName();
            PsiClass psiClass = getPsiClassFromClassAndFileNames(className, filePath);
            if(psiClass == null) {
                return;
            }
            PsiMethod psiMethod = getPsiMethod(psiClass, ((MoveRenameMethodObject) ref).getOriginalMethodSignature());
            if(psiMethod == null) {
                return;
            }
            psiElement = psiMethod;
        }
        else if(ref instanceof MoveRenameClassObject) {
            String filePath = ref.getOriginalFilePath();
            String className = ((MoveRenameClassObject) ref).getOriginalClassObject().getClassName();
            PsiClass psiClass = getPsiClassFromClassAndFileNames(className, filePath);
            if(psiClass == null) {
                return;
            }
            psiElement = psiClass;
        }
        else {
            return;
        }

        try {
            TextRange range = psiElement.getTextRange();
            Document document = PsiDocumentManager.getInstance(project).getCachedDocument(psiElement.getContainingFile());
            if (document != null) {
                ref.setStartLine(document.getLineNumber(range.getStartOffset()));
                ref.setEndLine(document.getLineNumber(range.getEndOffset()));
            }
        }
        catch(NullPointerException e) {
            e.printStackTrace();
            return;
        }
    }

}

