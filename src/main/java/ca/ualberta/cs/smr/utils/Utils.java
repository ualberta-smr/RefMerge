package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    /*
     * Runs a command such as "cp -r ..." or "git merge-files ..."
     */
    public static String runSystemCommand(String... commands) {
        StringBuilder builder = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /*
     * Save the content of one directory to another
     */
    public static void saveContent(Project project, String dir) throws IOException {
        // Save project to temporary directory
        String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("cp", "-r", project.getBasePath(), path);
    }


}
