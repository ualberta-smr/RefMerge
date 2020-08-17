package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.util.ArrayList;
import java.util.List;


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
        String rightCommit = "3db713ba";
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
        String path = "/home/mjellis/Projects/cloned/Rajawali";
        // Clear temp if not already cleared
      //  Utils.remove("/home/mjellis/temp");
        int i = 0;

        File dir = new File(path);
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
        undoRefactorings(rightCommit, baseCommit);
        // Save right content to memory
        saveContent(project, "right");
        gitUtils.checkout(leftCommit);
        // Undo left refactorings
        undoRefactorings(leftCommit, baseCommit);
        // Save left content to memory
        saveContent(project, "left");
        // Merge

        // Replay refactoring operations
  //      replayRefactorings(mergeCommit, baseCommit);


    }

    private void undoRefactorings(String commit, String baseCommit) {
        try {
            List<Refactoring> refs = detectCommits(commit, baseCommit);
            System.out.println(refs);
            System.out.println(refs.get(0).toString());
            System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");
            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_CLASS:
        //                undoRenameClass(ref);
                        break;
                    case MOVE_CLASS:
                        break;
                    case RENAME_METHOD:
                        undoRenameMethod(ref);
                        break;
                    case MOVE_OPERATION:
                        break;
                }

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void replayRefactorings(String commit, String baseCommit) {
        try {
            List<Refactoring> refs = detectCommits(commit, baseCommit);
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
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        PsiClass jClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));
        System.out.println(qualifiedClass);
        if(jPF == null) {
            System.out.println("NULL");
        }
        else {
            System.out.println("NOT NULL");
        }
        PsiMethod[] methods = jClass.getMethods();
        for(PsiMethod method : methods) {
            if(method.getName().equals(srcName)) {
                System.out.println("Method Name: " + method.getName());
                System.out.println(destName);
                RenameProcessor processor = new RenameProcessor(proj, method, destName, false, false);
                processor.run();
            }
        }

    }

    public void undoRenameMethod(Refactoring ref) {
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
        if(jClass == null) {
            System.out.println("Thing : " + qClass);
            qClass = qClass + ".java";
            System.out.println("Hello");
            // Work around?
            PsiFile[] pFiles = FilenameIndex.getFilesByName(proj, qClass, GlobalSearchScope.allScope(proj));
            PsiJavaFile pFile = (PsiJavaFile) pFiles[0];
            //       PsiJavaFile pFile = (PsiJavaFile) pM.findFile(vFile);
            PsiClass[] jClasses = pFile.getClasses();
            for (PsiClass it : jClasses) {
                System.out.println(it.getQualifiedName());
                if (it.getQualifiedName().equals(qualifiedClass)) {
                    jClass = it;
                }
            }
            PsiMethod[] methods = jClass.getMethods();
            for(PsiMethod method : methods) {
                if(method.getName().equals(destName)) {
                    System.out.println("Method Name: " + method.getName());
                    RenameProcessor processor = new RenameProcessor(proj, method, srcName, false, false);
                    processor.run();
                }
            }
        }
        // If the class is found
        else {
            System.out.println("Class: " + qualifiedClass);
            PsiMethod[] methods = jClass.getMethods();
            for(PsiMethod method : methods) {
                if(method.getName().equals(destName)) {
                    System.out.println("Method Name: " + method.getName());
                    RenameProcessor processor = new RenameProcessor(proj, method, srcName, false, false);
                    processor.run();
                }
            }
        }
    }

    private void undoRenameClass(Refactoring ref) {
        String srcClass = ((RenameClassRefactoring) ref).getOriginalClassName();
        String renamedClass = ((RenameClassRefactoring) ref).getRenamedClassName();
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);

        PsiClass jClass = jPF.findClass(renamedClass, GlobalSearchScope.allScope((proj)));
        RenameProcessor proc = new RenameProcessor(proj, jClass, srcClass, false, false);
        proc.run();
    }


    public List<Refactoring> detectCommits(String commit, String base) throws Exception {
        List<Refactoring> refResult = new ArrayList<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
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
        return refResult;
    }

    private void saveContent(Project project, String dir) throws IOException {
        // Save project to temporary directory using API
        String path = "/home/mjellis/temp/" + dir;
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        boolean isDir = file.mkdirs();
        if(isDir) {
            System.out.println("success");
        }
        else {
            System.out.println("error");
        }
        Utils.runSystemCommand(project.getBasePath(), "cp", "-r", project.getBasePath(), path);
    }



}