package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.evaluation.data.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.data.ConflictBlockData;
import ca.ualberta.cs.smr.evaluation.data.ConflictingFileData;
import ca.ualberta.cs.smr.evaluation.data.SourceFile;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.vcs.log.Hash;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.javalite.activejdbc.Base;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import ca.ualberta.cs.smr.core.RefMerge;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.File;
import java.util.*;

public class EvaluationPipeline implements ApplicationStarter {
    private Project project;

    @Override
    public String getCommandName() {
        return "evaluation";
    }

    @Override
    public void main(@NotNull List<String> args) {
        try {
            DatabaseUtils.createDatabase();
            String path = System.getProperty("user.home") + args.get(1);
            File pathToProject = new File(path);
            this.project = ProjectUtil.openOrImport(pathToProject.toPath(), null, false);
            GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
            List<GitRepository> repos = repoManager.getRepositories();
            GitRepository repo = repos.get(0);
            System.out.println(repo);
            startEvaluation(repo);
        } catch(Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private void startEvaluation(GitRepository repo) {
        try {
            Base.open();
            runEvaluation(repo);
            Base.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    private void runEvaluation(GitRepository repo) {
        // Add project to database
        String projectURL = repo.getPresentableUrl();
        String projectName = projectURL.substring(projectURL.lastIndexOf('/') + 1);
        ca.ualberta.cs.smr.evaluation.database.Project proj =
                ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectURL);
        if (proj == null) {
            proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectURL, projectName);
            proj.saveIt();
            evaluateProjectOnIntelliMergeDataset(proj, repo);
        } else if(!proj.isDone()){
            evaluateProjectOnIntelliMergeDataset(proj, repo);

        }
    }

    private void evaluateProjectOnIntelliMergeDataset(ca.ualberta.cs.smr.evaluation.database.Project proj, GitRepository repo) {

        Utils.clearTemp("manualMerge");
        Utils.clearTemp("intelliMerge");
        Utils.clearTemp("refMergeResults");
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("gitMergeResults");
        Utils.clearTemp("refMergeResultsOriginal");
        Utils.clearTemp("gitMergeResultsOriginal");
        Utils.clearTemp("intelliMergeResultsOriginal");
        String clonedDest = this.project.getBasePath();
        assert clonedDest != null;
        GitUtils git = new GitUtils(repo, project);
//        String mergeCommit = "e34f03bd0c7c805789bdb9da427db7334e61cedc"; // deeplearning4j
//        String mergeCommit = "588def5f5d92ba1e4ec5929dcaed4150a925a90b"; //undertow
//        String mergeCommitHash = "07559b47674594fdf40f2855f83b492f67f9093c"; //error-prone
//        String mergeCommitHash = "0e97a336019b2590a5a486cd4d0249a60db36eb7"; //error-prone 2
        List<String> mergeScenarios = new ArrayList<>();
        mergeScenarios.add("07559b47674594fdf40f2855f83b492f67f9093c");
        mergeScenarios.add("0e97a336019b2590a5a486cd4d0249a60db36eb7");
        mergeScenarios.add("1a87c33bd18648f484133794840e94bd8d1d4a64");
        mergeScenarios.add("f10f48c87d828881dda9912f050bccf8bb36776c");
        mergeScenarios.add("d51253011690def06db835d5ad605ca134c94d84");
        mergeScenarios.add("f25c38b51e0f35ea1637832e1ea6680c343203e2");
        mergeScenarios.add("66a86d1652c383ea3921d03f49f444f1d1000765");


        for (String mergeScenario : mergeScenarios) {
            evaluateMergeScenario(mergeScenario, git, repo, proj);
        }
        proj.setDone();
        proj.saveIt();

    }

    private void evaluateMergeScenario(String mergeCommitHash, GitUtils git, GitRepository repo,
                                       ca.ualberta.cs.smr.evaluation.database.Project proj) {
        git.checkout(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        String manuallyMergedPath = Utils.saveContent(project, "manualMerge");
        GitCommit targetCommit = git.getTargetMergeCommit(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Perform the merge with the three tools.
        List<Hash> parents = targetCommit.getParents();
        String rightParent = parents.get(0).toShortString();
        String leftParent = parents.get(1).toShortString();
        String baseCommit = git.getBaseCommit(leftParent, rightParent);

        // Get the refactorings detected by RefMiner in the merge scenario

        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        boolean isConflicting = gitUtils.merge(rightParent);
        // Add merge commit to database
        MergeCommit mergeCommit = MergeCommit.findFirst("commit_hash = ?", mergeCommitHash);
        if(mergeCommit == null) {
            mergeCommit = new MergeCommit(mergeCommitHash, isConflicting, leftParent,
                    rightParent, proj, targetCommit.getTimestamp());
            mergeCommit.saveIt();
        }
        else if(mergeCommit.isDone()) {
            return;
        }
        else if(!mergeCommit.isDone()) {
            mergeCommit.delete();
            mergeCommit = new MergeCommit(mergeCommitHash, isConflicting, leftParent,
                    rightParent, proj, targetCommit.getTimestamp());
            mergeCommit.saveIt();
        }

        // Get refactoring details
        getRefactorings(baseCommit, leftParent, project.getBasePath(), mergeCommit);
        getRefactorings(baseCommit, rightParent, project.getBasePath(), mergeCommit);

        // Merge the merge scenario with the three tools and record the runtime
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Run GitMerge
        long gitMergeRuntime = runGitMerge(project, repo, leftParent, rightParent);
        String gitMergePath = Utils.saveContent(project, "gitMergeResults");
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Run IntelliMerge
        String intelliMergePath = System.getProperty("user.home") + "/temp/intelliMergeResults";
        long intelliMergeRuntime = runIntelliMerge(project, repo, leftParent, baseCommit, rightParent, intelliMergePath);
        // Run RefMerge
        Pair<Integer, Long> refMergeConflictsAndRuntime = runRefMerge(project, repo, leftParent, rightParent);
        String refMergePath = Utils.saveContent(project, "refMergeResults");




        Utils.runSystemCommand("cp", "-r", refMergePath + "/.", refMergePath + "Original");
        Utils.runSystemCommand("cp", "-r", gitMergePath + "/.", gitMergePath + "Original");
        Utils.runSystemCommand("cp", "-r", intelliMergePath + "/.", intelliMergePath + "Original");

        // Remove all comments from all directories
        EvaluationUtils.removeAllComments(manuallyMergedPath);
        EvaluationUtils.removeAllComments(refMergePath);
        EvaluationUtils.removeAllComments(intelliMergePath);
        EvaluationUtils.removeAllComments(gitMergePath);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> refMergeConflicts =
                EvaluationUtils.extractMergeConflicts(refMergePath);
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> gitMergeConflicts =
                EvaluationUtils.extractMergeConflicts(gitMergePath);
        Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> intelliMergeConflicts =
                EvaluationUtils.extractMergeConflicts(intelliMergePath);

        // Get manually merged java files
        File manuallyMergedDir = new File(manuallyMergedPath);
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMergedDir);

        // Compare tools with manually merged code
        ComparisonResult refMergeVsManual = EvaluationUtils.compareAutoMerged(refMergePath, manuallyMergedFiles, project, repo);
        ComparisonResult gitVsManual = EvaluationUtils.compareAutoMerged(gitMergePath, manuallyMergedFiles, project, repo);
        ComparisonResult intelliMergeVsManual = EvaluationUtils.compareAutoMerged(intelliMergePath, manuallyMergedFiles, project, repo);

        System.out.println("Elapsed RefMerge runtime = " + refMergeConflictsAndRuntime);
        System.out.println("Elapsed Git merge runtime = " + gitMergeRuntime);
        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);
        System.out.println("Total RefMerge Conflicts: " + refMergeConflicts.getLeft());
        System.out.println("Total Git Merge Conflicts: " + gitMergeConflicts.getLeft());
        System.out.println("Total IntelliMerge Conflicts: " + intelliMergeConflicts.getLeft());

        System.out.println("RefMerge Statistics:\n#Different Files: " + refMergeVsManual.getTotalDiffFiles() + "\nPrecision: " +
                refMergeVsManual.getPrecision() + "\nRecall: " + refMergeVsManual.getRecall());
        System.out.println("Git Statistics:\n#Different Files: " + gitVsManual.getTotalDiffFiles() + "\nPrecision: " +
                gitVsManual.getPrecision() + "\nRecall: " + gitVsManual.getRecall());
        System.out.println("IntelliMerge Statistics:\n#Different Files: " + intelliMergeVsManual.getTotalDiffFiles() + "\nPrecision: " +
                intelliMergeVsManual.getPrecision() + "\nRecall: " + intelliMergeVsManual.getRecall());

        // Add RefMerge data to database
        int refactoringConflicts = refMergeConflictsAndRuntime.getLeft();
        int totalConflicts = refactoringConflicts + refMergeConflicts.getLeft().getLeft();
        MergeResult refMergeResult = new MergeResult("RefMerge", totalConflicts, refMergeConflicts.getLeft().getRight(),
                refMergeConflictsAndRuntime.getRight(), refMergeVsManual, mergeCommit);
        refMergeResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> refMergeConflictingFiles = refMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : refMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(refMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }

        // Add Git data to database
        MergeResult gitMergeResult = new MergeResult("Git", gitMergeConflicts.getLeft().getLeft(),
                gitMergeConflicts.getLeft().getRight(), gitMergeRuntime, gitVsManual, mergeCommit);
        gitMergeResult.saveIt();
            // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> gitMergeConflictingFiles = gitMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(gitMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }


        // Add IntelliMerge data to database
        MergeResult intelliMergeResult = new MergeResult("IntelliMerge", intelliMergeConflicts.getLeft().getLeft(),
                intelliMergeConflicts.getLeft().getRight(), intelliMergeRuntime, intelliMergeVsManual, mergeCommit);
        intelliMergeResult.saveIt();
        // Add conflicting files to database
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflictingFiles = intelliMergeConflicts.getRight();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflictingFiles) {
            ConflictingFile conflictingFile = new ConflictingFile(intelliMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }

        mergeCommit.setDone();
        mergeCommit.saveIt();

    }

    /*
     * Merge the left and right parent using RefMerge. Return how long it takes for RefMerge to finish
     */
    private Pair<Integer, Long> runRefMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        RefMerge refMerging = new RefMerge();
        System.out.println("Starting RefMerge");
        long time = System.currentTimeMillis();
        int conflicts = 0;
        try {
            conflicts = refMerging.refMerge(leftParent, rightParent, project, repo);
        }
        catch(AssertionError e) {
            e.printStackTrace();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("RefMerge is done");
        return Pair.of(conflicts, time2 - time);
    }

    /*
     * Merge the left and right parent using Git. Return how long it takes for Git to finish
     */
    private long runGitMerge(Project project, GitRepository repo, String leftParent, String rightParent) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        long time = System.currentTimeMillis();
        gitUtils.merge(rightParent);
        long time2 = System.currentTimeMillis();
        return time2 - time;
    }

    /*
     * Merge the left and right parent using IntelliMerge via command line. Return how long it takes for IntelliMerge
     * to finish
     */
    private long runIntelliMerge(Project project, GitRepository repo, String leftParent, String baseCommit,
                                 String rightParent, String output) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.checkout(leftParent);
        String leftPath = Utils.saveContent(project, "intelliMerge/ours");
        gitUtils.checkout(baseCommit);
        String basePath = Utils.saveContent(project, "intelliMerge/base");
        gitUtils.checkout(rightParent);
        String rightPath = Utils.saveContent(project, "intelliMerge/theirs");
        String jarFile =  System.getProperty("user.home") + "/temp/IntelliMerge-1.0.7-all.jar";
        System.out.println("Starting IntelliMerge");
        long time = System.currentTimeMillis();
        try {
            Utils.runSystemCommand("java", "-jar", jarFile, "-d", leftPath, basePath, rightPath, "-o", output);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        long time2 = System.currentTimeMillis();
        System.out.println("IntelliMerge is done");
        return time2 - time;
    }

    /*
     * Get each refactoring between the base commit and a parent commit for the merge scenario.
     */
    private void getRefactorings(String base, String parent, String path, MergeCommit mergeCommit) {
        File pathDir = new File(path);
        try {
            Git git = Git.open(pathDir);
            GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
            miner.detectBetweenCommits(git.getRepository(), base, parent,
                    new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            for (Refactoring refactoringObject : refactorings) {
                                String refactoringType = refactoringObject.getName();
                                String refactoringDetail = refactoringObject.toString();
                                if(refactoringDetail.length() > 2000) {
                                    refactoringDetail = refactoringDetail.substring(0, 1900);
                                }
                                ca.ualberta.cs.smr.evaluation.database.Refactoring refactoring =
                                        new ca.ualberta.cs.smr.evaluation.database.Refactoring(
                                                refactoringType,
                                                refactoringDetail,
                                                commitId,
                                                mergeCommit);
                                refactoring.saveIt();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}