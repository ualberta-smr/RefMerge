package ca.ualberta.cs.smr.evaluation.database;

import org.javalite.activejdbc.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.common.Util.blank;

/**
 * A class to create the evaluation database if it does not exist.
 * @author  Mehran Mahmoudi
 */
public class DatabaseUtils {
    private static final String CREATE_COMPARISON_SCHEMA_FILE = "/create_comparison_schema.sql";
    private static final String CREATE_REPLICATION_SCHEMA_FILE = "/create_replication_schema.sql";
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DELIMITER_KEYWORD = "DELIMITER";
    private static final String[] COMMENT_CHARS = new String[]{"--", "#", "//"};


    public static void createDatabase(boolean isComparison) throws Exception {
        try {
            if(isComparison) {
                Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/refMerge_evaluation",
                        "root", "password");
            }
            else {
                Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/intelliMerge_replication",
                        "root", "password");
            }
            Base.close();

        } catch (InitException e) {
            DB db = new DB("create_db").open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost",
                    "root", "password");

            String dbName;
            if(isComparison) {
                dbName = "refMerge_evaluation";
            }
            else {
                dbName = "intelliMerge_replication";
            }
            if(isComparison) {
                URL scriptInputStream = DatabaseUtils.class.getResource(CREATE_COMPARISON_SCHEMA_FILE);
                DatabaseUtils.createDatabase(scriptInputStream.openStream(), db, "refMerge_evaluation", dbName);
            }
            else {
                URL scriptInputStream = DatabaseUtils.class.getResource(CREATE_REPLICATION_SCHEMA_FILE);
                DatabaseUtils.createDatabase(scriptInputStream.openStream(), db, "intelliMerge_replication", dbName);
            }
            db.close();
        }

    }

    private static void createDatabase(InputStream scriptInputStream, DB db, String defaultDbName, String newDbName)
            throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(scriptInputStream));
        String delimiter = DEFAULT_DELIMITER;
        List<String> statements = new ArrayList<>();
        String currentStatement = "";
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim().replace(defaultDbName, newDbName);
            if (!commentLine(line) && !blank(line)) {
                if (line.startsWith(DELIMITER_KEYWORD)) {
                    delimiter = line.substring(10).trim();
                } else if (line.endsWith(delimiter)) {
                    currentStatement += line.substring(0, line.length() - delimiter.length());
                    if (!blank(currentStatement)) {
                        statements.add(currentStatement);
                    }
                    currentStatement = "";
                } else {
                    currentStatement += line + System.getProperty("line.separator");
                }
            }
        }
        try {
            reader.close();
            scriptInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!blank(currentStatement)) {
            statements.add(currentStatement);
        }

        for (String statement : statements) {
            db.exec(statement);
        }
    }

    private static boolean commentLine(String line) {
        for (String cc : COMMENT_CHARS) {
            if (line.trim().startsWith(cc)) {
                return true;
            }
        }
        return false;
    }


}
