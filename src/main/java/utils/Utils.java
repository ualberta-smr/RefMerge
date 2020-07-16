package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static String runSystemCommand(String dir, String... commands) {
        StringBuilder builder = new StringBuilder();
        try {
            Runtime rt = Runtime.getRuntime();
            System.out.println(dir);
            Process proc = rt.exec(commands, null, new File(dir));

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
//                if (verbose) log(s);
            }

            while ((s = stdError.readLine()) != null) {
                builder.append(s);
                builder.append("\n");
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


}
