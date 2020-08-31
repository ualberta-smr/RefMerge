package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.ide.UiActivity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.api.Git;
import ca.ualberta.cs.smr.utils.GitUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RefMerge extends AnAction {

    Git git;
    Project proj;

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }
// New Example: multiple rename methods
    // Project: core
    // URL: https://github.com/MasDennis/Rajawali
    // merge commit: 98787ef5
    // parent 1: 3d9b713ba
    // parent 2: 5e7fcebe4
    // base: 773d48939a2ccba
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.proj = ProjectManager.getInstance().getOpenProjects()[0];
        Project project = proj;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
        String mergeCommit = "98787ef5";
        String rightCommit = "3d9b713ba";
        String leftCommit = "5e7fcebe4";
        String baseCommit = "773d48939a2ccba";
        try {
            refMerge(mergeCommit, rightCommit, leftCommit, baseCommit, project, repo);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (VcsException vcsException) {
            vcsException.printStackTrace();
        }
    }


    public void refMerge(String mergeCommit, String rightCommit, String leftCommit, String baseCommit, Project project,
                         GitRepository repo) throws IOException, VcsException {
        // Clear temp if not already cleared
        //  Utils.remove("/home/mjellis/temp");
        int i = 0;

        File dir = new File(project.getBasePath());
        try {
            git = Git.open(dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        doMerge(mergeCommit, rightCommit, leftCommit, baseCommit, project, repo);

    }

    private void doMerge(String mergeCommit, String rightCommit, String leftCommit, String baseCommit, Project project,
                         GitRepository repo) throws IOException, VcsException {

        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(baseCommit);
        // Save base content to memory
        saveContent(project, "base");
        gitUtils.checkout(rightCommit);
        // Undo right refactorings
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }
        List<Refactoring> refs = undoRefactorings(rightCommit, baseCommit, project);
        // Save right content to memory
        saveContent(project, "right");
        gitUtils.checkout(leftCommit);
        // Undo left refactorings
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }
        //
        refs.addAll(undoRefactorings(leftCommit, baseCommit, project));
        // Merge
        Merge merge = new Merge(project);
        merge.merge();

        // Replay refactoring operations
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }

        //replayRefactorings(refs);


    }

    private List<Refactoring> undoRefactorings(String commit, String baseCommit, Project project) {


        List<Refactoring> refs = detectCommits(commit, baseCommit);
        System.out.println(refs);
        System.out.println(refs.get(0).toString());
        System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");

        for(Refactoring ref : refs) {
            switch (ref.getRefactoringType()) {
                case RENAME_CLASS:
                    undoRenameClass(ref);
                    break;
                case MOVE_CLASS:
                    break;
                case RENAME_METHOD:
                    undoRenameMethod(ref, project);
                    break;
                case MOVE_OPERATION:
                    break;
            }

        }
        FileDocumentManager.getInstance().saveAllDocuments();


        return refs;
    }

    private void replayRefactorings(List<Refactoring> refs) {
        try {
            System.out.println(refs);
            System.out.println(refs.get(0).toString());
            System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");
            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_CLASS:
                        break;
                    case MOVE_CLASS:
                        break;
                    case RENAME_METHOD:
                        replayRenameMethod(ref);
                        break;
                    case MOVE_OPERATION:
                        break;
                }

            }
            FileDocumentManager.getInstance().saveAllDocuments();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void replayRenameMethod(Refactoring ref) {
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

    private void undoRenameClass(Refactoring ref) {
        System.out.println(ref.toString());
        String srcClass = ((RenameClassRefactoring) ref).getOriginalClassName();
        String renamedClass = ((RenameClassRefactoring) ref).getRenamedClassName();
        String renamedClassName = renamedClass.substring(renamedClass.lastIndexOf(".") + 1).trim() + ".java";
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        PsiClass jClass = jPF.findClass(renamedClass, GlobalSearchScope.allScope((proj)));
        if(jClass == null) {
            System.out.println(renamedClassName);
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, renamedClassName, GlobalSearchScope.allScope(proj));
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            PsiClass[] jClasses = pFile.getClasses();

        }
        RenameProcessor proc = new RenameProcessor(proj, jClass, srcClass, false, false);
        proc.run();
    }


    public List<Refactoring> detectCommits(String commit, String base) {
        List<Refactoring> refResult = new ArrayList<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            miner.detectBetweenCommits(git.getRepository(), base, commit,
                    new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            System.out.println("Refactorings at " + commitId);
                            for (Refactoring ref : refactorings) {
                                refResult.add(ref);
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return refResult;
    }

    private void saveContent(Project project, String dir) throws IOException {
        // Save project to temporary directory using API
        String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        boolean isDir = file.mkdirs();
        if(isDir) {
            System.out.println("success");
        }
        else {
            System.out.println("error");
        }
        Utils.runSystemCommand("cp", "-r", project.getBasePath(), path);
    }




}