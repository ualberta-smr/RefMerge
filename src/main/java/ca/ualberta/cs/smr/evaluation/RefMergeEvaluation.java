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
        URL url = EvaluationPipeline.class.getResource("/intelliMerge_data");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        GitRepository repo = null;
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            String[] values = line.split(";");
            if(!values[0].contains("error-prone") && !values[0].contains("junit")) {
                continue;
            }
            if(!values[0].equals(projectUrl)) {
                if(proj != null) {
                    if(!proj.isDone()) {
                        proj.setDone();
                        proj.saveIt();
                    }
                }
                projectUrl = values[0];
                proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
                if (proj == null) {
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
                        repo = repoManager.getRepositoryForFile(virtualFile);
                    }
                    else {
                        repo = repos.get(0);
                    }
                }
            }
            evaluateMergeScenario(values[1], repo, proj);
        }
        if(proj != null) {
            if(!proj.isDone()) {
                proj.setDone();
                proj.saveIt();
            }
        }
    }



    /*
     * Run RefMerge, IntelliMerge, and Git on the given merge scenario.
     */
    private void evaluateMergeScenario(String mergeCommitHash, GitRepository repo,
                                       ca.ualberta.cs.smr.evaluation.database.Project proj) {
        GitUtils git = new GitUtils(repo, project);

        Utils.clearTemp("manualMerge");
        Utils.clearTemp("intelliMerge");
        Utils.clearTemp("refMergeResults");
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("gitMergeResults");
        Utils.clearTemp("refMergeResultsOriginal");
        Utils.clearTemp("gitMergeResultsOriginal");
        Utils.clearTemp("intelliMergeResultsOriginal");

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
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run GitMerge
        long gitMergeRuntime = runGitMerge(project, repo, leftParent, rightParent);
        String gitMergePath = Utils.saveContent(project, "gitMergeResults");
        DumbService.getInstance(project).completeJustSubmittedTasks();
        // Run IntelliMerge
        String intelliMergePath = System.getProperty("user.home") + "/temp/intelliMergeResults";
        long intelliMergeRuntime = runIntelliMerge(project, repo, leftParent, baseCommit, rightParent, intelliMergePath);
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
        refMergeResultDirectory.mkdirs();
        gitResultDirectory.mkdirs();
        intelliMergeResultDirectory.mkdirs();
        Utils.runSystemCommand("cp", "-r", refMergePath + "/.", refMergeResultDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", gitMergePath + "/.", gitResultDirectory.getAbsolutePath());
        Utils.runSystemCommand("cp", "-r", intelliMergePath + "/.", intelliMergeResultDirectory.getAbsolutePath());

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
        List<Pair<RefactoringObject, RefactoringObject>> refactoringConflicts = refMergeConflictsAndRuntime.getLeft();
        int totalConflicts = refactoringConflicts.size() + refMergeConflicts.getLeft().getLeft();
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
        // Add refactoring conflict data to database
        for(Pair<RefactoringObject, RefactoringObject> pair : refactoringConflicts) {
            RefactoringConflict refactoringConflict = new RefactoringConflict(pair.getLeft(), pair.getRight(), refMergeResult);
            refactoringConflict.saveIt();
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
    private long runIntelliMerge(com.intellij.openapi.project.Project project, GitRepository repo, String leftParent, String baseCommit,
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
