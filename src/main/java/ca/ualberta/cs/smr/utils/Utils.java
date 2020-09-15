package ca.ualberta.cs.smr.utils;

import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static String runSystemCommand(String... commands) {
        //public static String runSystemCommand(String dir, String... commands) {
        StringBuilder builder = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process p = pb.start();
            p.waitFor();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
                System.out.println(s);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static void saveContent(Project project, String dir) throws IOException {
        // Save project to temporary directory using API
        String path = System.getProperty("user.home") + "/temp/" + dir;
        File file = new File(path);
        System.out.println(file.getAbsolutePath());
        boolean isDir = file.mkdirs();
        if(isDir) {
            System.out.println("success");
        }
        else {
            System.out.println("error");
        }
        Utils.runSystemCommand("cp", "-r", project.getBasePath(), path);
    }


}
