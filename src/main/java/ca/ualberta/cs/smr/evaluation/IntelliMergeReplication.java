package ca.ualberta.cs.smr.evaluation;

import ca.ualberta.cs.smr.utils.EvaluationUtils;
import ca.ualberta.cs.smr.utils.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class IntelliMergeReplication {

    /*
     * Use the IntelliMerge dataset to try to replicate the IntelliMerge results with both versions of IntelliMerge.
     */
    public static void runIntelliMergeReplication(String path) throws IOException {
        Utils.clearTemp("projects");
        URL url = EvaluationPipeline.class.getResource("/intelliMerge_data");
        InputStream inputStream = url.openStream();
        ArrayList<String> lines = getLinesFromInputStream(inputStream);
        String projectUrl = "";
        String projectName = "";
        ca.ualberta.cs.smr.evaluation.database.Project proj = null;
        for(String line : lines) {
            String[] values = line.split(";");
            if(!values[0].equals(projectUrl)) {
                if(proj != null) {
                    if(!proj.isDone()) {
                        proj.setDone();
                        proj.saveIt();
                    }
                }
                projectUrl = values[0];
                projectName = projectUrl.substring(projectUrl.lastIndexOf("/") + 1);
                proj = ca.ualberta.cs.smr.evaluation.database.Project.findFirst("url = ?", projectUrl);
                if (proj == null) {
                    System.out.println("Starting " + projectName);
                    proj = new ca.ualberta.cs.smr.evaluation.database.Project(projectUrl, projectName);
                    proj.saveIt();
                    cloneProject(path, projectUrl);
                }
            }
            replicateMergeScenario(values[1], path + "/" + projectName);
        }
        if(proj != null) {
            if(!proj.isDone()) {
                proj.setDone();
                proj.saveIt();
            }
        }
        Utils.clearTemp("projects");
    }

    /*
     * Run both versions of IntelliMerge on the given merge scenario.
     */
    private static void replicateMergeScenario(String mergeCommitHash, String path) throws IOException {
        Utils.clearTemp("intelliMergeResults");
        Utils.clearTemp("manualMerge");
        File file = new File(path);
        Git git = Git.open(file);
        EvaluationUtils.checkout(git, mergeCommitHash);
        Repository repository = git.getRepository();
        ObjectId id = repository.resolve(mergeCommitHash);
        RevCommit commit = git.getRepository().parseCommit(id);


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

}
