package ca.ualberta.cs.smr.core;


import com.intellij.openapi.project.Project;
import ca.ualberta.cs.smr.utils.Utils;

import java.io.File;
import java.util.ArrayList;

/*
 * Perform git merge on most of the files using git merge-file
 */

public class Merge {
    Project project;

    public Merge(Project project) {
        this.project = project;
    }

    /*
     * Performs the merge using the temporary directories and project directory
     */
    public void merge() {
        // Directory of project
        String ours = project.getBasePath();
        // Get the users home directory for the path
        String home = System.getProperty("user.home");
        // Temporary right directory
        String theirs = home + "/temp/right/" + project.getName();
        // Temporary base directory
        String base = home + "/temp/base/" + project.getName();
        // Get the relative paths in the base directory
        ArrayList<String> baseFiles = getFiles(base, new ArrayList<>());
        // Get the relative paths in the left directory
        ArrayList<String> ourFiles = getFiles(ours, new ArrayList<>());
        // Get the relative paths in the right directory
        ArrayList<String> theirFiles = getFiles(theirs, new ArrayList<>());

        // For each file in the left directory
        for(String file : ourFiles) {
            // If it's in the base and right directory
            if (baseFiles.contains(file) && theirFiles.contains(file)) {
                // Get the full path for each file
                String ourFile = ours + file;
                String theirFile = theirs + file;
                String baseFile = base  + file;
                // merge the set of files
                String cmd = "git merge-file " + ourFile + " " + baseFile + " " + theirFile;
                Utils.runSystemCommand("git", "merge-file", ourFile, baseFile, theirFile);
            }
        }

    }

    /*
     * Get the relative path for each file in a directory
     */
    private ArrayList<String> getFiles(String dirs, ArrayList<String> files) {
        String name = project.getName();
        // Get the length of the project's name
        int length = name.length();
        File dir = new File(dirs);
        // Get a list of files in the directory
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Get the absolute path of the file
                String absPath = child.getAbsolutePath();
                String path = null;
                // If the file is not a directory
                if(child.isFile()) {
                    // Get the relative path
                    path = absPath.substring(absPath.indexOf(project.getName()) + length);
                    // Add the relative path to the list
                    files.add(path);
                }
                // If it's a directory
                else if(child.isDirectory()) {
                    // Avoid hidden directories
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
