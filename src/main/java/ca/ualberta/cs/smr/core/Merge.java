package ca.ualberta.cs.smr.core;


import com.intellij.openapi.project.Project;
import ca.ualberta.cs.smr.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class Merge {
    Project project;

    public Merge(Project project) {
        this.project = project;
    }

    public void merge() {
        String d = project.getBasePath();
        System.out.println(d);
        String ours = project.getBasePath();
        String home = System.getProperty("user.home");
        String theirs = home + "/temp/right";
        String base = home + "/temp/base";
        ArrayList<String> baseFiles = getFiles(base, new ArrayList<>());
        ArrayList<String> ourFiles = getFiles(ours, new ArrayList<>());
        ArrayList<String> theirFiles = getFiles(theirs, new ArrayList<>());


    }

    private ArrayList<String> getFiles(String dirs, ArrayList<String> files) {

        File dir = new File(dirs);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String absPath = child.getAbsolutePath();
                String path;
                if(child.isFile()) {
                    if(dirs.contains(project.getName())) {
                        path = absPath.substring(absPath.indexOf(project.getName()));

                    } else if(dirs.contains("/base/")) {
                        path = absPath.substring(absPath.indexOf("/base") + 5);
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
