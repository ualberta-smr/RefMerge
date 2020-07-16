package refactoring.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.psi.*;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.rename.RenameProcessor;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameVariableRefactoring;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.api.Git;
import refactoring.core.Merge;
import utils.GitUtils;
import utils.Utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class RefMerge extends AnAction {

    Git git;
    Project proj;

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.proj = ProjectManager.getInstance().getOpenProjects()[0];
        Project project = proj;
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);
        String mergeCommit = "d4a7c3660";
        String rightCommit = "4f0374d8";
        String leftCommit = "24617f7c";
        String baseCommit = "0f4357067";
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
        String path = "/home/mjellis/Projects/cloned/platform_packages_apps_email";
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
        saveContent(project, "theirs");
        gitUtils.checkout(leftCommit);
        // Undo left refactorings
        undoRefactorings(leftCommit, baseCommit);
        // Merge

        // Replay refactoring operations
        replayRefactorings(mergeCommit, baseCommit);


    }

    private void undoRefactorings(String commit, String baseCommit) {
        try {
            List<RenameProcessor> processors = new ArrayList<RenameProcessor>();
            List<Refactoring> refs = detectCommits(commit, baseCommit);
            System.out.println(refs);
            System.out.println(refs.get(0).toString());
            System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");
            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_METHOD:
                        processors.add(undoRenameMethod(ref));
                        break;
                }

            }
            for(RenameProcessor processor : processors) {
                processor.run();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void replayRefactorings(String commit, String baseCommit) {
        try {
            List<RenameProcessor> processors = new ArrayList<RenameProcessor>();
            List<Refactoring> refs = detectCommits(commit, baseCommit);
            System.out.println(refs);
            System.out.println(refs.get(0).toString());
            System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");
            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_METHOD:
                        processors.add(replayRenameMethod(ref));
                        break;
                }

            }
            for(RenameProcessor processor : processors) {
                processor.run();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private RenameProcessor replayRenameMethod(Refactoring ref) {
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
                return processor;
            }
        }
        return null;
    }

    public RenameProcessor undoRenameMethod(Refactoring ref) {
        String refS = ref.toString();
        System.out.println(refS);

        String destName = refS.substring(refS.indexOf("to") + 3, refS.indexOf("(", refS.indexOf("(") + 1));
        destName = destName.substring(destName.indexOf(" ") + 1, destName.length());
        String srcName = refS.substring(refS.indexOf("\t"), refS.indexOf("("));
        srcName = srcName.split(" ")[1];
        String qualifiedClass = refS.substring(refS.indexOf("class ") + 6, refS.length());
        JavaPsiFacade jPF = new JavaPsiFacadeImpl(proj);
        PsiClass jClass = jPF.findClass(qualifiedClass, GlobalSearchScope.allScope(proj));
        PsiMethod[] methods = jClass.getMethods();
        for(PsiMethod method : methods) {
            if(method.getName().equals(destName)) {
                System.out.println("Method Name: " + method.getName());
                RenameProcessor processor = new RenameProcessor(proj, method, srcName, false, false);
                return processor;
            }
        }
        return null;
    }


    public List<Refactoring> detectCommits(String right, String base) throws Exception {

        List<Refactoring> refResult = new ArrayList<>();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        miner.detectBetweenCommits(git.getRepository(), base, right,
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
    }



}