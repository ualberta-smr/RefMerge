package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.evaluation.model.SourceFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

/*
 * Contains the methods necessary to get the metrics for the evaluation.
 */
public class EvaluationUtils {

    /*
     * Get the number of merge conflicts as well as each conflict block.
     */
    public static Pair<Integer, String> extractMergeConflicts(String directory) {
        ArrayList<SourceFile> temp = new ArrayList<>();
        File dir = new File(directory);
        ArrayList<SourceFile> mergedFiles = getJavaSourceFiles(directory, temp, dir);
        Integer totalConflicts = 0;

        return Pair.of(totalConflicts, null);
    }

    /*
     * Get the merged java files from the directory.
     */
    private static ArrayList<SourceFile> getJavaSourceFiles(String path, ArrayList<SourceFile> javaSourceFiles, File root) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    getJavaSourceFiles(f.getAbsolutePath(), javaSourceFiles, root);
                } else if (f.isFile() && isJavaFile(f)) {
                    String absolutePath = f.getAbsolutePath();
                    String relativePath = Path.of(root.toURI()).relativize(Path.of(f.toURI())).toString();
                    String fileName = f.getName();
                    SourceFile sourceFile =
                            new SourceFile(fileName, relativePath, absolutePath);
                    javaSourceFiles.add(sourceFile);
                }
            }
        } else {
            System.out.println(path + " does not exist!");
        }
        return javaSourceFiles;

    }

    /*
     * Check if the given file is a java file.
     */
    private static boolean isJavaFile(File file) {
        return file.getName().toLowerCase().contains(".java");
    }

}
