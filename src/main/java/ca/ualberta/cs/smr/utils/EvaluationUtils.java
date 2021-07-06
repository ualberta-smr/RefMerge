package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.evaluation.model.ConflictBlock;
import ca.ualberta.cs.smr.evaluation.model.ConflictingFile;
import ca.ualberta.cs.smr.evaluation.model.SourceFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Contains the methods necessary to get the metrics for the evaluation.
 */
public class EvaluationUtils {

    public static final String CONFLICT_LEFT_BEGIN = "<<<<<<<";
    public static final String CONFLICT_RIGHT_BEGIN = "=======";
    public static final String CONFLICT_RIGHT_END = ">>>>>>>";


    /*
     * Get the number of merge conflicts as well as each conflicting file.
     */
    public static Pair<Integer, List<ConflictingFile>> extractMergeConflicts(String directory) {
        ArrayList<SourceFile> temp = new ArrayList<>();
        File dir = new File(directory);
        ArrayList<SourceFile> mergedFiles = getJavaSourceFiles(directory, temp, dir);
        List<ConflictingFile> conflictingFiles = new ArrayList<>();
        Integer totalConflicts = 0;
        for(SourceFile file : mergedFiles) {
            int conflictingLOC = 0;
            List<ConflictBlock> conflictBlocks = extractConflictBlocks(file.getAbsolutePath());
            // Get the total number of conflict blocks in the file
            totalConflicts += conflictBlocks.size();
            for(ConflictBlock conflictBlock : conflictBlocks) {
                conflictingLOC += conflictBlock.getEndLine() - conflictBlock.getStartLine();
                System.out.println(file.getAbsolutePath());
                System.out.println(conflictBlock.getStartLine());
                System.out.println(conflictBlock.getEndLine());
                System.out.println(conflictBlock.getLeft());
                System.out.println(conflictBlock.getRight());
            }
            // If the file is conflicting
            if(conflictBlocks.size() > 0) {
                ConflictingFile conflictingFile = new ConflictingFile(file.getAbsolutePath(), conflictBlocks.size(), conflictingLOC);
                conflictingFiles.add(conflictingFile);
            }
        }

        return Pair.of(totalConflicts, conflictingFiles);
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

    /*
     * Extract the individual merge conflicts from the file and record their information.
     */
    private static List<ConflictBlock> extractConflictBlocks(String path) {
        String leftConflictingContent = "";
        String rightConflictingContent = "";
        boolean isConflictOpen = false;
        boolean isLeftContent = false;
        int lineCounter = 0;
        int startLOC = 0;
        int endLOC = 0;

        List<ConflictBlock> mergeConflicts = new ArrayList<>();
        List<String> lines = readFileToLines(path);
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            lineCounter++;
            if (line.contains(CONFLICT_LEFT_BEGIN)) {
                isConflictOpen = true;
                isLeftContent = true;
                startLOC = lineCounter;
            } else if (line.contains(CONFLICT_RIGHT_BEGIN)) {
                isLeftContent = false;
            } else if (line.contains(CONFLICT_RIGHT_END)) {
                endLOC = lineCounter;
                ConflictBlock mergeConflict =
                        new ConflictBlock(leftConflictingContent, rightConflictingContent, startLOC, endLOC);
                mergeConflicts.add(mergeConflict);
                // reset the flags
                isConflictOpen = false;
                isLeftContent = false;
                leftConflictingContent = "";
                rightConflictingContent = "";
            } else {
                if (isConflictOpen) {
                    if (isLeftContent) {
                        leftConflictingContent += line + "\n";
                    } else {
                        rightConflictingContent += line + "\n";
                    }
                }
            }
        }
        return mergeConflicts;

    }

    /*
     * Read the content of a given file with the file encoding 'UTF-8' as a list of lines.
     */
    public static List<String> readFileToLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (file.exists()) {
            String fileEncoding = "UTF-8";
            try (BufferedReader reader =
                         Files.newBufferedReader(Paths.get(path), Charset.forName(fileEncoding))) {
                lines = reader.lines().collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(path + " does not exist!");
        }
        return lines;
    }


}
