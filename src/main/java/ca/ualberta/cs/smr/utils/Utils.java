package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;

public class Utils {

    /*
     * Runs a command such as "cp -r ..." or "git merge-files ..."
     */
    public static void runSystemCommand(String... commands) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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

    /*
     * Remove the temp files
     */
    public static void clearTemp() throws IOException {
        String path = System.getProperty("user.home") + "/temp/right";
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("rm", "-rf", path);
        path = System.getProperty("user.home") + "/temp/left";
        runSystemCommand("rm", "-rf", path);
        path = System.getProperty("user.home") + "/temp/base";
        runSystemCommand("rm", "-rf", path);
    }

    public static void dumbServiceHandler(Project project) {
        if(DumbService.isDumb(project)) {
            DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
            // Waits for the task to finish
            dumbService.completeJustSubmittedTasks();
        }
    }
}

