package refactoring.core;


import com.intellij.openapi.project.Project;
import utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class Merge {
    public void merge(Project project) {
        String d = project.getBasePath();
        System.out.println(d);
        String ours = "left";
        String theirs = "right";
        String base = "base";
        String merged = "merged";
        ArrayList<String> baseFiles = getFiles(base, new ArrayList<>());
        ArrayList<String> ourFiles = getFiles(ours, new ArrayList<>());
        ArrayList<String> theirFiles = getFiles(theirs, new ArrayList<>());
/*      for(String file : ourFiles) {
            System.out.println(file);
            System.exit(0);
        }
*/      for(String file : baseFiles) {

            if (ourFiles.contains(file) && theirFiles.contains(file)) {
                String ourFile = "left" + file;
                String theirFile = "right" + file;
                String baseFile = "base" + file;
                String mergedFile = "merged" + file;
                System.out.println(ourFile);
//              String cmd = "git merge-file -p " + ourFile + " " + baseFile + " " + theirFile + " > " + mergedFile;
                String cmd = "git merge-file " + ourFile + " " + baseFile + " " + theirFile;
                Utils.runSystemCommand(d, cmd);
                System.out.println(d);
            }
        }

    }

    private ArrayList<String> getFiles(String dirs, ArrayList<String> files) {

        File dir = new File(dirs);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String absPath = child.getAbsolutePath();
                String path = null;
                if(child.isFile()) {
                    if(dirs.contains("/left")) {
                        path = absPath.substring(absPath.indexOf("/left/") + 5, absPath.length());

                    } else if(dirs.contains("/base/")) {
                        path = absPath.substring(absPath.indexOf("/base") + 5, absPath.length());
                        //System.out.println(path);
                    }
                    else {
                        path = absPath.substring(absPath.indexOf("/right") + 6);
                    }
                    files.add(path);
                }
                else if(child.isDirectory()) {
/*                      if(dirs.contains("/base/")) {
                            path = absPath.substring(absPath.indexOf("/base") + 5, absPath.length());
                            System.out.println(path);
                            String mergedFile = "merged" + path;
                            File mFile = new File(mergedFile);
                            System.out.println(mFile.getAbsolutePath());
                            boolean made = mFile.mkdir();
                            if(!made) {
                                System.exit(0);
                            }
                        }
*/                      getFiles(child.getAbsolutePath(), files);
                }
            }
        }
        return files;

    }





}
