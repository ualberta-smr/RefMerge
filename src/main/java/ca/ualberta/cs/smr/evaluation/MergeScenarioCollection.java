package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.utils.GitUtils;
import ca.ualberta.cs.smr.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
 * Get every merge scenario with at least one refactoring
 */
public class MergeScenarioCollection {

    public static List<Pair<String, Integer>> collectScenarios(String path) throws IOException {
        URL url = EvaluationPipeline.class.getResource("/refMerge_evaluation_projects");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        List<Pair<String, Integer>> pairs = new ArrayList<>();
        String statsPath = System.getProperty("user.home") + "/temp/refactoringStatistics.csv";
        Utils.writeContent(statsPath, "Project;Merge Scenarios");
            for(String s : lines) {
                    pairs.add(cloneAndAnalyzeProject(path, s, statsPath));

            }

        Utils.clearTemp("projects");
        return pairs;
    }

    private static Pair<String, Integer> cloneAndAnalyzeProject(String path, String projectUrl, String statsPath) {
        String projectName = projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
        Utils.log(null, "Cloning project " + projectName + "...");
        cloneProject(path, projectUrl);
        int count = 0;
        try {
            count = getTotalScenarios(path + "/" + projectName, projectName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.log(projectName, projectName + " has " + count + " merge scenarios with refactorings");
        Utils.writeContent(statsPath, projectName + ";" + count);
        Utils.log(projectName, "Finished project " + projectName + "...");
        return Pair.of(projectName, count);
    }

    /*
     * Analyze the cloned project with RefMiner to detect all merge scenarios with at least one refactoring.
     */
    private static int getTotalScenarios(String path, String projectName) throws IOException {
        Git git = Git.open(new File(path));
        Iterable<RevCommit> mergeScenarios = GitUtils.getMergeScenarios(git);
        int count = 0;
        int mergeScenarioNum = 0;
        for(RevCommit mergeScenario : mergeScenarios) {
            mergeScenarioNum++;
            Utils.log(null, projectName + ": Analyzing Merge Scenario: " + mergeScenario.getName() +
                    "(" + mergeScenarioNum + ")");
            // Work around base commit always being null
            if(checkForRefactorings(mergeScenario, git)) {
                count++;
            }

        }
        return count;
    }

    /*
     * Check for refactorings in the given merge scenario.
     */
    private static boolean checkForRefactorings(RevCommit mergeScenario, Git git) {
            try {
                Repository repo = git.getRepository();
                ObjectId id = repo.resolve(mergeScenario.getName());
                RevCommit finalMergeScenario = repo.parseCommit(id);
                RevCommit leftParent = finalMergeScenario.getParent(0);
                RevCommit rightParent = finalMergeScenario.getParent(1);
                String baseCommit = GitUtils.getBaseCommit(leftParent, rightParent, git.getRepository());
                if (baseCommit == null) {
                    return  false;
                }
                else if (hasRefactoring(baseCommit, rightParent.getName(), repo)) {
                    return true;
                } else if (hasRefactoring(baseCommit, leftParent.getName(), repo)) {
                    return true;
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return false;
    }

    /*
     * Check if there is at least one refactoring between the base commit and the parent.
     */
    private static boolean hasRefactoring(String baseCommit, String parentCommit, Repository repo) {
        final boolean[] foundRefactoring = {false};
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future futureRefMiner = executor.submit(() -> {
            try {
                GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
                miner.detectBetweenCommits(repo, baseCommit, parentCommit,
                        new RefactoringHandler() {
                            @Override
                            public void handle(String commitId, List<org.refactoringminer.api.Refactoring> refactorings) {
                                if (refactorings.size() > 0) {
                                    foundRefactoring[0] = true;
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            // Wait up to 1 minutes for RefactoringMiner to finish its analysis.
            futureRefMiner.get(1, TimeUnit.MINUTES);


        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            Utils.log(null, String.format("Commit %.7s timed out. Skipping...", parentCommit));
        }
        return foundRefactoring[0];
    }

    /*
     * Clone the given project.
     */
    private static void cloneProject(String path, String url) {
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
     * Get each line from the input stream containing the IntelliMerge dataset.
     */
    private static ArrayList<String> getLinesFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }

}
