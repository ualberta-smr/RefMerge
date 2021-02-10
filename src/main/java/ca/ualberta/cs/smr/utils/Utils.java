package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    public static void saveContent(Project project, String dir) {
        // Save project to temporary directory
        String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(path);
        file.mkdirs();
        runSystemCommand("cp", "-r", project.getBasePath(), path);
    }

    /*
     * Remove the temp files
     */
    public static void clearTemp() {
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

    public static void reparsePSIClasses(Project project) {
        File file = new File(project.getBasePath());
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        assert virtualFile != null;
        VirtualFile[] vFileArray = virtualFile.getChildren();
        ArrayList<VirtualFile> vFileCollection = new ArrayList<>(Arrays.asList(vFileArray));
        vFileCollection.add(virtualFile);
        FileContentUtil.reparseFiles(project, vFileCollection, true);
    }
}

