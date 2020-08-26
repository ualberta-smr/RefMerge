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
        String ours = project.getBasePath();
        String home = System.getProperty("user.home");
        String theirs = home + "/temp/right/" + project.getName();
        String base = home + "/temp/base/" + project.getName();
        ArrayList<String> baseFiles = getFiles(base, new ArrayList<>());
        ArrayList<String> ourFiles = getFiles(ours, new ArrayList<>());
        ArrayList<String> theirFiles = getFiles(theirs, new ArrayList<>());

        for(String file : ourFiles) {
            if (baseFiles.contains(file) && theirFiles.contains(file)) {
                String ourFile = ours + file;
                String theirFile = theirs + file;
                String baseFile = base  + file;
                String cmd = "git merge-file " + ourFile + " " + baseFile + " " + theirFile;

                Utils.runSystemCommand("git", "merge-file", ourFile, baseFile, theirFile);
            }
        }
    }

    private ArrayList<String> getFiles(String dirs, ArrayList<String> files) {
        String name = project.getName();
        int length = name.length();

        File dir = new File(dirs);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String absPath = child.getAbsolutePath();
                String path = null;
                if(child.isFile()) {
                    path = absPath.substring(absPath.indexOf(project.getName()) + length);
                    files.add(path);
                }
                else if(child.isDirectory()) {
                    if(absPath.contains("/.")) {
                        return files;
                    }
                      getFiles(child.getAbsolutePath(), files);
                }
            }
        }
        return files;

    }





}
