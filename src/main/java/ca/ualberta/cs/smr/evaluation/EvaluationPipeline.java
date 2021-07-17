package ca.ualberta.cs.smr.evaluation;


import ca.ualberta.cs.smr.evaluation.database.*;

import com.intellij.openapi.application.ApplicationStarter;
import org.javalite.activejdbc.Base;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
                startEvaluation(path);
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
    private void startEvaluation(String path) {
        try {
            Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/refMerge_evaluation",
                    "root", "password");
            RefMergeEvaluation evaluation = new RefMergeEvaluation();
            evaluation.runComparison(path);
            Base.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }



}