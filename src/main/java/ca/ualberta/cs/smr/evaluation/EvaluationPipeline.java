package ca.ualberta.cs.smr.evaluation;


import ca.ualberta.cs.smr.evaluation.database.*;

import com.intellij.openapi.application.ApplicationStarter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.javalite.activejdbc.Base;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/*
 * Evaluates RefMerge and IntelliMerge. If in replication mode, The pipeline will attempt to replicate IntelliMerge's
 * results. If in comparison mode, the pipeline will run a comparison on RefMerge, IntelliMerge, and Git. If in
 * stats mode, it will run RefMiner and collect merge scenarios that contain refactorings.
 */
public class EvaluationPipeline implements ApplicationStarter {

    @Override
    public String getCommandName() {
        return "evaluation";
    }

    @Override
    public void main(@NotNull List<String> args) {
        try {
            String mode = args.get(1);
            if(mode.equals("replication")) {
                DatabaseUtils.createDatabase(false);
                String path = System.getProperty("user.home") + args.get(2);
                startIntelliMergeReplication(path);
            }
            else if(mode.equals("comparison")) {
                DatabaseUtils.createDatabase(true);
                String path = System.getProperty("user.home") + args.get(2);
                String projectName = args.get(3);
                startEvaluation(path, projectName);
            }
        } catch(Throwable e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /*
     * Start the IntelliMerge replication.
     */
    private void startIntelliMergeReplication(String path) {
        try {
            Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/intelliMerge_replication",
                    "root", "password");
            IntelliMergeReplication.runIntelliMergeReplication(path);
            Base.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /*
     * Start the head to head comparison between RefMerge and IntelliMerge.
     */
    private void startEvaluation(String path, String evaluationProject) {
        try {
            Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/refMerge_evaluation",
                    "root", "password");
            RefMergeEvaluation evaluation = new RefMergeEvaluation();
            evaluation.runComparison(path, evaluationProject);
            Base.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void collectAndPrintMergeScenarios(String path) {
        try {
            List<Pair<String, Integer>> pairs = MergeScenarioCollection.collectScenarios(path);
            for(Pair<String, Integer> pair : pairs) {
                System.out.println(pair.getLeft() + ":  " + pair.getRight());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}