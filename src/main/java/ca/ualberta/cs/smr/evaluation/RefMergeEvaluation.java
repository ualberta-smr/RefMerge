package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.core.RefMerge;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.evaluation.data.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.data.ConflictBlockData;
import ca.ualberta.cs.smr.evaluation.data.ConflictingFileData;
import ca.ualberta.cs.smr.evaluation.data.SourceFile;
import ca.ualberta.cs.smr.evaluation.database.*;
import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.Hash;
import edu.pku.intellimerge.client.IntelliMerge;
import git4idea.GitCommit;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RefMergeEvaluation {
    private com.intellij.openapi.project.Project project;

    public RefMergeEvaluation() {
        this.project = null;
    }

    /*
     * Use the given git repository to evaluate IntelliMerge, RefMerge, and Git.
     */
    public void runComparison(String path) throws IOException {
        //Utils.clearTemp("projects");
        URL url = EvaluationPipeline.class.getResource("/refMerge_evaluation_projects");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        GitRepository repo = null;
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            projectUrl = line;
            if(!projectUrl.contains("error-prone")) {
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
            evaluateProject(repo, proj);
            proj.setDone();
            proj.saveIt();


        }
        if(proj != null) {
            if(!proj.isDone()) {
                proj.setDone();
                proj.saveIt();
            }
        }
    }

    /*
     * Evaluate every merge scenario in the given project.
     */
    private void evaluateProject(GitRepository repo, Project proj) {
        GitUtils gitUtils = new GitUtils(repo, project);
        List<GitCommit> mergeCommits = gitUtils.getMergeCommits();
        System.out.println(mergeCommits.size());
        for(GitCommit mergeCommit : mergeCommits) {
            evaluateMergeScenario(mergeCommit, repo, proj);
        }

    }

    /*
     * Run RefMerge, IntelliMerge, and Git on the given merge scenario.
     */
    private void evaluateMergeScenario(GitCommit targetCommit, GitRepository repo,
                                       ca.ualberta.cs.smr.evaluation.database.Project proj) {
        GitUtils gitUtils = new GitUtils(repo, project);
        gitUtils.reset();
        Utils.clearTemp("manualMerge");
        Utils.clearTemp("intelliMerge");
        Utils.clearTemp("refMergeResults");
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("gitMergeResults");
        Utils.clearTemp("refMergeResultsOriginal");
        Utils.clearTemp("gitMergeResultsOriginal");
        Utils.clearTemp("intelliMergeResultsOriginal");;

        String mergeCommitHash = targetCommit.getId().asString();
        gitUtils.checkout(mergeCommitHash);
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Save the manually merged version
        String manuallyMergedPath = Utils.saveContent(project, "manualMerge");
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        // Perform the merge with the three tools.
        List<Hash> parents = targetCommit.getParents();
        String rightParent = parents.get(0).toShortString();
        String leftParent = parents.get(1).toShortString();
        String baseCommit = gitUtils.getBaseCommit(leftParent, rightParent);

        // Skip cases without a base commit
        if(baseCommit == null) {
            return;
        }

        // Get the refactorings detected by RefMiner in the merge scenario

        gitUtils.checkout(leftParent);
        boolean isConflicting = gitUtils.merge(rightParent);
        if(isConflicting) {
            gitUtils.reset();
        }
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
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run GitMerge
        long gitMergeRuntime = runGitMerge(project, repo, leftParent, rightParent);
        String gitMergePath = Utils.saveContent(project, "gitMergeResults");
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run IntelliMerge
        String intelliMergePath = System.getProperty("user.home") + "/temp/intelliMergeResults";
        List<String> commits = new ArrayList<>();
        commits.add(leftParent);
        commits.add(baseCommit);
        commits.add(rightParent);
        long intelliMergeRuntime = runIntelliMerge(project.getBasePath(), commits, "intelliMergeResults");
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run RefMerge
        Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, Long> refMergeConflictsAndRuntime =
                runRefMerge(project, repo, leftParent, rightParent);
        String refMergePath = Utils.saveContent(project, "refMergeResults");
        DumbService.getInstance(project).completeJustSubmittedTasks();


        String resultDir = System.getProperty("user.home") + "/temp/results/" + project.getName() + "/" + "commit" + mergeCommit.getId();
        File refMergeResultDirectory = new File(resultDir + "/refMerge");
        File gitResultDirectory = new File(resultDir + "/git");
        File intelliMergeResultDirectory = new File(resultDir + "/intelliMerge");
        File manualMergeResultDirectory = new File(resultDir + "/manualMerge");
        refMergeResultDirectory.mkdirs();
        gitResultDirectory.mkdirs();
        intelliMergeResultDirectory.mkdirs();
        manualMergeResultDirectory.mkdirs();
        Utils.runSystemCommand("cp", "-r", refMergePath + "/.", refMergeResultDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", gitMergePath + "/.", gitResultDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", intelliMergePath + "/.", intelliMergeResultDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", manuallyMergedPath + "/.", manualMergeResultDirectory.getAbsolutePath());

        // Remove all comments from all directories
        EvaluationUtils.removeAllComments(manuallyMergedPath);
        EvaluationUtils.removeAllComments(refMergePath);
        EvaluationUtils.removeAllComments(intelliMergePath);
        EvaluationUtils.removeAllComments(gitMergePath);


        // Get the conflict blocks from each of the merged results as well as the number of conflict blocks
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> refMergeConflicts = EvaluationUtils
                .extractMergeConflicts(refMergePath, false);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> gitMergeConflicts = EvaluationUtils
                .extractMergeConflicts(gitMergePath, false);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflicts = EvaluationUtils
                .extractMergeConflicts(intelliMergePath, false);


        // Compare IntelliMerge and RefMerge conflict blocks for discrepancies
        EvaluationUtils.getSameConflicts(refMergeConflicts, intelliMergeConflicts);

        // Get manually merged files from git conflicts

        // Get manually merged java files
        File manuallyMergedDir = new File(manuallyMergedPath);
        ArrayList<SourceFile> manuallyMergedFiles = EvaluationUtils
                .getJavaSourceFiles(manuallyMergedPath, new ArrayList<>(), manuallyMergedDir);



        // Compare tools with manually merged code
        ComparisonResult refMergeVsManual = EvaluationUtils
                .compareAutoMerged(refMergePath, manuallyMergedFiles, project.getBasePath(), null, false);
        ComparisonResult gitVsManual = EvaluationUtils
                .compareAutoMerged(gitMergePath, manuallyMergedFiles, project.getBasePath(), null, false);
        ComparisonResult intelliMergeVsManual = EvaluationUtils
                .compareAutoMerged(intelliMergePath, manuallyMergedFiles, project.getBasePath(), null, false);

        System.out.println("Elapsed RefMerge runtime = " + refMergeConflictsAndRuntime);
        System.out.println("Elapsed Git merge runtime = " + gitMergeRuntime);
        System.out.println("Elapsed IntelliMerge runtime = " + intelliMergeRuntime);

        System.out.println("RefMerge Statistics:\n#Different Files: " + refMergeVsManual.getTotalDiffFiles() + "\nPrecision: " +
                refMergeVsManual.getPrecision() + "\nRecall: " + refMergeVsManual.getRecall());
        System.out.println("Git Statistics:\n#Different Files: " + gitVsManual.getTotalDiffFiles() + "\nPrecision: " +
                gitVsManual.getPrecision() + "\nRecall: " + gitVsManual.getRecall());
        System.out.println("IntelliMerge Statistics:\n#Different Files: " + intelliMergeVsManual.getTotalDiffFiles() + "\nPrecision: " +
                intelliMergeVsManual.getPrecision() + "\nRecall: " + intelliMergeVsManual.getRecall());

        // Add RefMerge data to database
        List<Pair<RefactoringObject, RefactoringObject>> refactoringConflicts = refMergeConflictsAndRuntime.getLeft();
        int totalConflictingLOC = 0;
        int totalConflicts = 0;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : refMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
        }
        totalConflicts += refactoringConflicts.size();
        MergeResult refMergeResult = new MergeResult("RefMerge", totalConflicts, totalConflictingLOC,
                refMergeConflictsAndRuntime.getRight(), refMergeVsManual, mergeCommit);
        refMergeResult.saveIt();
        // Add conflicting files to database;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : refMergeConflicts) {
            ConflictingFile conflictingFile = new ConflictingFile(refMergeResult, pair.getLeft());
            conflictingFile.saveIt();
            // Add each conflict block for the conflicting file
            for(ConflictBlockData conflictBlockData : pair.getRight()) {
                ConflictBlock conflictBlock = new ConflictBlock(conflictingFile, conflictBlockData);
                conflictBlock.saveIt();
            }
        }
        // Add refactoring conflict data to database
        for(Pair<RefactoringObject, RefactoringObject> pair : refactoringConflicts) {
            RefactoringConflict refactoringConflict = new RefactoringConflict(pair.getLeft(), pair.getRight(), refMergeResult);
            refactoringConflict.saveIt();
        }

        // Add Git data to database
        totalConflictingLOC = 0;
        totalConflicts = 0;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : gitMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
        }
        MergeResult gitMergeResult = new MergeResult("Git", totalConflicts, totalConflictingLOC,
                gitMergeRuntime, gitVsManual, mergeCommit);
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
        totalConflictingLOC = 0;
        totalConflicts = 0;
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
            totalConflicts += pair.getRight().size();
            totalConflictingLOC += pair.getLeft().getConflictingLOC();
        }
        MergeResult intelliMergeResult = new MergeResult("IntelliMerge", totalConflicts, totalConflictingLOC,
                intelliMergeRuntime, intelliMergeVsManual, mergeCommit);
        intelliMergeResult.saveIt();
        // Add conflicting files to database
        for(Pair<ConflictingFileData, List<ConflictBlockData>> pair : intelliMergeConflicts) {
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
    private Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, Long> runRefMerge(com.intellij.openapi.project.Project project,
                                                                                          GitRepository repo,
                                                                                          String leftParent,
                                                                                          String rightParent) {
        RefMerge refMerging = new RefMerge();
        System.out.println("Starting RefMerge");
        long time = System.currentTimeMillis();
        ArrayList<Pair<RefactoringObject, RefactoringObject>> conflicts = new ArrayList<>();
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
    private long runGitMerge(com.intellij.openapi.project.Project project, GitRepository repo, String leftParent, String rightParent) {
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
    private long runIntelliMerge(String repoPath, List<String> commits, String output) {
        String outputDir = System.getProperty("user.home") + "/temp/" + output;
        System.out.println("Starting IntelliMerge");
        long time = System.currentTimeMillis();
        try {
            IntelliMerge merge = new IntelliMerge();
            // our commit, base commit, their commit in that order
            List<Long> times = merge.mergeBranchesForRefMergeEvaluation(repoPath, commits, outputDir, true);
            long time2 = System.currentTimeMillis();
            System.out.println("Collection took: " + times.get(0));
            System.out.println("Building took: " + times.get(1));
            System.out.println("Matching took: " + times.get(2));
            System.out.println("Merging took: " + times.get(3));
            return time2 - time;
        } catch(Exception e) {
            e.printStackTrace();

        }
        System.out.println("IntelliMerge is done");
        return -1;
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
            if(this.project != null) {
                ProjectUtil.closeAndDispose(this.project);
            }
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
     * Get each line from the input stream containing the IntelliMerge dataset.
     */
    private ArrayList<String> getLinesFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
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
                        public void handle(String commitId, List<org.refactoringminer.api.Refactoring> refactorings) {
                            for (org.refactoringminer.api.Refactoring refactoringObject : refactorings) {
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
