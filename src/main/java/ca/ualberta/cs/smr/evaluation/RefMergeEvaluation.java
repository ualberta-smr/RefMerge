package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.core.RefMerge;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.evaluation.data.*;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import edu.pku.intellimerge.client.IntelliMerge;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.refactoringminer.api.Refactoring;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RefMergeEvaluation {
    private com.intellij.openapi.project.Project project;

    public RefMergeEvaluation() {
        this.project = null;
    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    public void runComparison(String path, String evaluationProject) throws IOException {
        URL url = EvaluationPipeline.class.getResource("/refMerge_evaluation_projects");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = Utils.getLinesFromInputStream(inputStream);
        String projectUrl;
        String projectName;
        GitRepository repo;
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            projectUrl = line;
            if(!line.contains(evaluationProject)) {
                continue;
            }
            proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
            if(proj == null) {
                projectName = openProject(path, projectUrl).substring(1);
                System.out.println("Starting " + projectName);
                proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectUrl, projectName);
                proj.saveIt();
                GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
                List<GitRepository> repos = repoManager.getRepositories();
                if(repos.size() == 0) {
                    // Why does this fail?
                    // Does it load the project properly?
                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path + "/" + projectName + "/.git");
                    GitRepositoryManager.getInstance(project).updateRepository(virtualFile);
                    assert virtualFile != null;
                    repo = repoManager.getRepositoryForFile(virtualFile);
                }
                else {
                    repo = repos.get(0);
                }
            }
            else if(proj.isDone()) {
                continue;
            }
            else {
                projectName = openProject(path, projectUrl).substring(1);
                System.out.println("Continuing " + projectName);
                GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
                List<GitRepository> repos = repoManager.getRepositories();
                repo = repos.get(0);
            }
            evaluateProject(repo, proj, projectName);
            proj.setDone();
            proj.saveIt();


        }
    }

    /*
     * Evaluate merge scenarios with refactoring-involved conflicts in the given project.
     */
    private void evaluateProject(GitRepository repo, Project proj, String projectName) throws IOException {
        URL url = EvaluationPipeline.class.getResource("/refMerge_evaluation_commits");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = Utils.getLinesFromInputStream(inputStream);
        int i = 0;
        for(String line : lines) {
            String[] values = line.split(";");
            if(values[0].contains(projectName)) {
                System.out.println("Evaluating on " + ++i + ": " + values[1]);
                evaluateMergeScenario(values, repo, proj);
            }
        }

    }

    /*
     * Run RefMerge, IntelliMerge, and Git on the given merge scenario.
     */
    private void evaluateMergeScenario(String[] values, GitRepository repo,
                                   ca.ualberta.cs.smr.evaluation.database.Project proj) {

        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.reset();
        String tempPath = System.getProperty("user.home") + "/temp/";
        Utils.clearTemp(tempPath + "manualMerge");
        Utils.clearTemp(tempPath + "intelliMerge");

        String mergeCommitHash = values[1];
        MergeCommit mergeCommit = MergeCommit.findFirst("commit_hash = ?", mergeCommitHash);
        if(mergeCommit != null && mergeCommit.isDone()) {
            return;
        }



        gitUtils.checkout(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        String rightParent = values[2];
        String leftParent = values[3];
        String baseCommit = gitUtils.getBaseCommit(leftParent, rightParent);

        // Skip cases without a base commit
        if (baseCommit == null) {
            return;
        }

        gitUtils.checkout(leftParent);
        boolean isConflicting = gitUtils.merge(rightParent);
        if(!isConflicting) {
            // This should always be conflicting
            System.out.println("Error merging with Git");
            return;
        }
        // Add merge commit to database
        if (mergeCommit == null) {
            mergeCommit = new MergeCommit(mergeCommitHash, isConflicting, leftParent,
                    rightParent, proj, Long.parseLong(values[4]));
            mergeCommit.saveIt();
        } else if (mergeCommit.isDone()) {
            return;
        } else if (!mergeCommit.isDone()) {
            mergeCommit.delete();
            mergeCommit = new MergeCommit(mergeCommitHash, isConflicting, leftParent,
                    rightParent, proj, Long.parseLong(values[4]));
            mergeCommit.saveIt();
        }
        String resultDir = "/mnt/DATA/temp/results/" + project.getName() + "/" + "commit" + mergeCommit.getId();

        String refMergePath = resultDir + "/refMerge";
        String gitMergePath = resultDir + "/git";
        String intelliMergePath = resultDir + "/intelliMerge";


        // Remove unmerged and non-java files from Git and RefMerge results to save space
        // Use project path
        EvaluationUtils.removeUnmergedAndNonJavaFiles(project.getBasePath());

        Utils.saveContent(project, gitMergePath);
        gitUtils.reset();


        // Merge the merge scenario with the three tools and record the runtime
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run IntelliMerge
        List<String> commits = new ArrayList<>();
        commits.add(leftParent);
        commits.add(baseCommit);
        commits.add(rightParent);
        long intelliMergeRuntime = runIntelliMerge(project.getBasePath(), commits, intelliMergePath, mergeCommitHash);
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run RefMerge
        Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, Long> refMergeConflictsAndRuntime =
                runRefMerge(project, repo, leftParent, rightParent, mergeCommit);

        EvaluationUtils.removeUnmergedAndNonJavaFiles(project.getBasePath());
        Utils.saveContent(project, refMergePath);
        DumbService.getInstance(project).completeJustSubmittedTasks();


        File refMergeConflictDirectory = new File(resultDir + "/refMergeResults");
        File gitConflictDirectory = new File(resultDir + "/gitResults");
        File intelliMergeConflictDirectory = new File(resultDir + "/intelliMergeResults");
        refMergeConflictDirectory.mkdirs();
        gitConflictDirectory.mkdirs();
        intelliMergeConflictDirectory.mkdirs();

        Utils.runSystemCommand("cp", "-r", refMergePath + "/.", refMergeConflictDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", gitMergePath + "/.", gitConflictDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", intelliMergePath + "/.", intelliMergeConflictDirectory.getAbsolutePath());



        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> refMergeConflicts = EvaluationUtils
                .extractMergeConflicts(refMergePath, "RefMerge", true);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> gitMergeConflicts = EvaluationUtils
                .extractMergeConflicts(gitMergePath, "Git", true);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflicts = EvaluationUtils
                .extractMergeConflicts(intelliMergePath, "IntelliMerge", true);

        List<String> relativePaths = new ArrayList<>();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> gitConflictFiles : gitMergeConflicts) {
            relativePaths.add(gitConflictFiles.getLeft().getFilePath());
        }

        // Compare IntelliMerge and RefMerge conflict blocks for discrepancies
        EvaluationUtils.getSameConflicts(refMergeConflicts, intelliMergeConflicts);

        System.out.println("Elapsed RefMerge runtime = " + refMergeConflictsAndRuntime);
        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);

        int totalConflictingLOC = 0;
        int totalConflicts = 0;
        int totalConflictingFiles = 0;
        // If RefMiner or RefMerge timeout
        if(refMergeConflictsAndRuntime.getRight() < 0) {
            MergeResult refMergeResult = new MergeResult("RefMerge", -1, -1, -1, -1, mergeCommit);
            refMergeResult.saveIt();
        }
        // Add RefMerge data to database
        else {
            List<Pair<RefactoringObject, RefactoringObject>> refactoringConflicts = refMergeConflictsAndRuntime.getLeft();
            List<String> files = new ArrayList<>();
            for (Pair<ConflictingFileData, List<ConflictBlockData>> pair : refMergeConflicts) {
                totalConflicts += pair.getRight().size();
                totalConflictingLOC += pair.getLeft().getConflictingLOC();
                if(!files.contains(pair.getLeft().getFilePath())) {
                    files.add((pair.getLeft().getFilePath()));
                    totalConflictingFiles++;
                }

            }
            totalConflicts += refactoringConflicts.size();
            MergeResult refMergeResult = new MergeResult("RefMerge", totalConflictingFiles, totalConflicts, totalConflictingLOC,
                    refMergeConflictsAndRuntime.getRight(), mergeCommit);
            refMergeResult.saveIt();
            // Add conflicting files to database;
            for (Pair<ConflictingFileData, List<ConflictBlockData>> pair : refMergeConflicts) {
                ConflictingFile conflictingFile = new ConflictingFile(refMergeResult, pair.getLeft());
                conflictingFile.saveIt();
                // Add each conflict block for the conflicting file
                for (ConflictBlockData conflictBlockData : pair.getRight()) {
                    ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                    conflictBlock.saveIt();
                }
            }

            // Add refactoring conflict data to database
            for (Pair<RefactoringObject, RefactoringObject> pair : refactoringConflicts) {
                RefactoringConflict refactoringConflict = new RefactoringConflict(pair.getLeft(), pair.getRight(), refMergeResult);
                refactoringConflict.saveIt();
            }
        }

        // Add Git data to database
        totalConflictingLOC = 0;
        totalConflicts = 0;
        totalConflictingFiles = 0;
        List<String> files = new ArrayList<>();
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
            if(!files.contains(pair.getLeft().getFilePath())) {
                files.add((pair.getLeft().getFilePath()));
                totalConflictingFiles++;
            }
        }
        MergeResult gitMergeResult = new MergeResult("Git", totalConflictingFiles, totalConflicts, totalConflictingLOC, 0, mergeCommit);
        gitMergeResult.saveIt();
        // Add conflicting files to database
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflicts) {
            ConflictingFile conflictingFile = new ConflictingFile(gitMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }


        // Add IntelliMerge data to database
        if(intelliMergeRuntime < 0) {
            MergeResult intelliMergeResult = new MergeResult("IntelliMerge", -1, -1, -1, -1, mergeCommit);
            intelliMergeResult.saveIt();
        }
        else {
            totalConflictingLOC = 0;
            totalConflicts = 0;
            totalConflictingFiles = 0;
            files = new ArrayList<>();
            for (Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
                totalConflicts += pair.getRight().size();
                totalConflictingLOC += pair.getLeft().getConflictingLOC();
                if(!files.contains(pair.getLeft().getFilePath())) {
                    files.add((pair.getLeft().getFilePath()));
                    totalConflictingFiles++;
                }
            }
            MergeResult intelliMergeResult = new MergeResult("IntelliMerge", totalConflictingFiles, totalConflicts, totalConflictingLOC,
                    intelliMergeRuntime, mergeCommit);
            intelliMergeResult.saveIt();
            // Add conflicting files to database
            for (Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
                ConflictingFile conflictingFile = new ConflictingFile(intelliMergeResult, pair.getLeft());
                conflictingFile.saveIt();
                // Add each conflict block for the conflicting file
                for (ConflictBlockData conflictBlockData : pair.getRight()) {
                    ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                    conflictBlock.saveIt();
                }
            }

        }

        Utils.clearTemp(gitMergePath);
        Utils.clearTemp(intelliMergePath);
        Utils.clearTemp(refMergePath);
        // Save space since we can perform a git merge easily to see results
        Utils.clearTemp(gitConflictDirectory.getAbsolutePath());

        // If RefMerge and IntelliMerge both timed out, free additional space
        if(intelliMergeRuntime < 0 || refMergeConflictsAndRuntime.getRight() < 0) {
            Utils.clearTemp(resultDir);
        }

        mergeCommit.setDone();
        mergeCommit.saveIt();
    }

    /*
     * Merge the left and right parent using RefMerge. Return how long it takes for RefMerge to finish
     */
    private Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, Long> runRefMerge(com.intellij.openapi.project.Project project,
                                                                                          GitRepository repo,
                                                                                          String leftParent,
                                                                                          String rightParent,
                                                                                          MergeCommit mergeCommit) {
        ArrayList<Pair<RefactoringObject, RefactoringObject>> conflicts = new ArrayList<>();
        List<org.refactoringminer.api.Refactoring> refactorings = new ArrayList<>();
        RefMerge refMerging = new RefMerge();
        System.out.println("Starting RefMerge");
        long time = System.currentTimeMillis();
        try {
            conflicts = refMerging.refMerge(leftParent, rightParent, project, repo, refactorings);
        }
        catch(AssertionError | OutOfMemoryError | LargeObjectException.OutOfMemory e) {
            e.printStackTrace();
        }
        long time2 = System.currentTimeMillis();
        // If RefMerge crashes
        if(conflicts == null || (time2 - time) > 799999) {
            time = -1;
            System.out.println("RefMerge timed out");
            return Pair.of(new ArrayList<>(), time);
        }
        System.out.println("RefMerge is done");
        recordRefactorings(refactorings, mergeCommit);
        return Pair.of(conflicts, time2 - time);
    }

    /*
     * Merge the left and right parent using IntelliMerge via command line. Return how long it takes for IntelliMerge
     * to finish
     */
    private long runIntelliMerge(String repoPath, List<String> commits, String output, String mergeCommit) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(() -> {
            IntelliMerge merge = new IntelliMerge();
            try {
                merge.mergeBranchesForRefMergeEvaluation(repoPath, commits, output, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long time2 = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        try {
            System.out.println("Starting IntelliMerge");
            future.get(15, TimeUnit.MINUTES);
            time2 = System.currentTimeMillis();
            System.out.println("IntelliMerge is done");
            return time2 - time;
        } catch (InterruptedException | ExecutionException | TimeoutException | OutOfMemoryError e) {
            e.printStackTrace();
            return -1;
        }
    }


    /*
     * Clone the given project.
     */
    private void cloneProject(String path, String url) {
        String projectName = url.substring(url.lastIndexOf("/"));
        String clonePath = path + projectName;
        try {
            Git.cloneRepository().setURI(url).setDirectory(new File(clonePath)).call();
        }
        catch(GitAPIException | JGitInternalException e) {
            e.printStackTrace();
        }
    }

    /*
     * Open the given project.
     */
    private String openProject(String path, String url) {
        String projectName = url.substring(url.lastIndexOf("/"));
        File pathToProject = new File(path + projectName);

        try {
            if(!pathToProject.exists()) {
                cloneProject(path, url);
            }
            this.project = ProjectUtil.openOrImport(pathToProject.toPath(), null, false);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return projectName;

    }

    /*
     * Record each refactoring that was detected by RefactoringMiner
     */
    private void recordRefactorings(List<Refactoring> refactorings, MergeCommit mergeCommit) {
        for(Refactoring refactoring : refactorings) {
            String refactoringType = refactoring.getRefactoringType().toString();
            String refactoringDetail = refactoring.toString();
            if(refactoringDetail.length() > 1999) {
                refactoringDetail = refactoringDetail.substring(0, 1999);
            }
            ca.ualberta.cs.smr.evaluation.database.Refactoring refactoringRecord =
                    new ca.ualberta.cs.smr.evaluation.database.Refactoring(refactoringType, refactoringDetail, mergeCommit);
            refactoringRecord.saveIt();
        }
    }

}
