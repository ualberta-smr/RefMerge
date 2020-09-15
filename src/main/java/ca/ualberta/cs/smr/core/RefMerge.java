package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
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
import java.util.concurrent.TimeUnit;


public class RefMerge extends AnAction {

    Git git;
    Project proj;

    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }
    // Example: multiple rename methods
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
        String mergeCommit = "a1b94c2"; //"98787ef5";
        String rightCommit = "dc37ff7"; //"3d9b713ba";
        String leftCommit = "f3a05c1"; //"5e7fcebe4";
        String baseCommit = "773ef6b"; //"773d48939a2ccba";
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
        int i = 0;

        File dir = new File(project.getBasePath());
        try {
            git = Git.open(dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        doMerge(rightCommit, leftCommit, baseCommit, project, repo);

    }

    private void doMerge(String rightCommit, String leftCommit, String baseCommit, Project project,
                         GitRepository repo) throws IOException, VcsException {

        GitUtils gitUtils = new GitUtils(repo, project);
        List<Refactoring> rightRefs = detectCommits(rightCommit, baseCommit);
        List<Refactoring> leftRefs = detectCommits(leftCommit, baseCommit);
        Matrix.runMatrix(leftRefs, rightRefs);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gitUtils.checkout(baseCommit);
        Utils.saveContent(project, "base");
        gitUtils.checkout(rightCommit);
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }
        undoRefactorings(rightRefs, project);
        Utils.saveContent(project, "right");
        gitUtils.checkout(leftCommit);
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }
        undoRefactorings(leftRefs, project);
        Merge merge = new Merge(project);
        merge.merge();

        // Replay refactoring operations
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            dumbService.completeJustSubmittedTasks();
        }
        leftRefs.addAll(rightRefs);
        replayRefactorings(leftRefs);


    }

    private List<Refactoring> undoRefactorings(List<Refactoring> refs, Project project) {

        System.out.println(refs);
        System.out.println(refs.get(0).toString());
        System.out.println(refs.get(0).getInvolvedClassesBeforeRefactoring() + " Before");

        UndoOperations undo = new UndoOperations(project);

        for(Refactoring ref : refs) {
            switch (ref.getRefactoringType()) {
                case RENAME_CLASS:
                    //undo.undoRenameClass(ref);
                    break;
                case MOVE_CLASS:
                    break;
                case RENAME_METHOD:
                    undo.undoRenameMethod(ref, project);
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

            ReplayOperations replay = new ReplayOperations(proj);

            for(Refactoring ref : refs) {
                switch (ref.getRefactoringType()) {
                    case RENAME_CLASS:
                        break;
                    case MOVE_CLASS:
                        break;
                    case RENAME_METHOD:
                        replay.replayRenameMethod(ref);
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




}