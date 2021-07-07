package ca.ualberta.cs.smr.evaluation.database;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpec;

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
    private static final String CREATE_SCHEMA_FILE = "/create_schema.sql";
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DELIMITER_KEYWORD = "DELIMITER";
    private static final String[] COMMENT_CHARS = new String[]{"--", "#", "//"};


    public static void createDatabase() throws Exception {
        try {
            Base.open();
            Base.close();
        } catch (InitException e) {
            Configuration config = Registry.instance().getConfiguration();
            ConnectionSpec spec = config.getCurrentConnectionSpec();
            if (!(spec instanceof ConnectionJdbcSpec)) {
                throw new DBException("Could not find configuration in a property file for environment: " +
                        config.getEnvironment() + ". Are you sure you have a database.properties file configured?");
            }

            ConnectionJdbcSpec jdbcSpec = (ConnectionJdbcSpec) spec;
            DB db = new DB("create_db").open(jdbcSpec.getDriver(),
                    jdbcSpec.getUrl().substring(0,
                            Math.min(jdbcSpec.getUrl().indexOf("/", 13), jdbcSpec.getUrl().length())),
                    jdbcSpec.getUser(), jdbcSpec.getPassword());

            String dbName = jdbcSpec.getUrl().substring(jdbcSpec.getUrl().indexOf("/", 13) + 1);
            URL scriptInputStream = DatabaseUtils.class.getResource(CREATE_SCHEMA_FILE);
            DatabaseUtils.createDatabase(scriptInputStream.openStream(), db, "refMerge_evaluation", dbName);
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
