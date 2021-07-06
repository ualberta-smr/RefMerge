package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.evaluation.model.ComparisonResult;
import ca.ualberta.cs.smr.evaluation.model.ConflictBlock;
import ca.ualberta.cs.smr.evaluation.model.ConflictingFile;
import ca.ualberta.cs.smr.evaluation.model.SourceFile;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
        int totalConflicts = 0;
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
    public static ArrayList<SourceFile> getJavaSourceFiles(String path, ArrayList<SourceFile> javaSourceFiles, File root) {
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
        StringBuilder leftConflictingContent = new StringBuilder();
        StringBuilder rightConflictingContent = new StringBuilder();
        boolean inConflictBlock = false;
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
                inConflictBlock = true;
                isLeftContent = true;
                startLOC = lineCounter;
                iterator.remove();
            } else if (line.contains(CONFLICT_RIGHT_BEGIN)) {
                isLeftContent = false;
                iterator.remove();
            } else if (line.contains(CONFLICT_RIGHT_END)) {
                endLOC = lineCounter;
                ConflictBlock mergeConflict =
                        new ConflictBlock(leftConflictingContent.toString(), rightConflictingContent.toString(), startLOC, endLOC);
                mergeConflicts.add(mergeConflict);
                // reset the flags
                inConflictBlock = false;
                isLeftContent = false;
                leftConflictingContent = new StringBuilder();
                rightConflictingContent = new StringBuilder();
                iterator.remove();
            } else {
                if (inConflictBlock) {
                    if (isLeftContent) {
                        leftConflictingContent.append(line).append("\n");
                    } else {
                        rightConflictingContent.append(line).append("\n");
                    }
                    iterator.remove();
                }
            }
        }
        // If there are merge conflicts, remove them for precision and recall calculations
        if (mergeConflicts.size() > 0) {
            writeLinesToFile(path, lines);
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

    /*
     * Combine the lines into one content string so we can write it to the file.
     */
    public static void writeLinesToFile(String path, List<String> lines) {
        String content =
                lines.stream().filter(line -> line.length() > 0).collect(Collectors.joining("\n"));
        writeContent(path, content);
    }

    /*
     * Write the
     */
    public static void writeContent(String filePath, String content) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(filePath, false);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Compare the auto-merged result of the provided tool with the manually merged result to determine the precision
     * and recall.
     */
    public static ComparisonResult compareAutoMerged(String mergedDir, List<SourceFile> manuallyMergedFiles) {

        int numberOfMergedFiles = manuallyMergedFiles.size();
        int numberOfDiffFiles = 0;
        Double autoMergePrecision = 0.0;
        Double autoMergeRecall = 0.0;
        Integer totalAutoMergedLOC = 0;
        Integer totalManualMergedLOC = 0;
        Integer totalSameLOCMerged = 0;
        Integer totalSameLOCManual = 0;

        // For each manually merged Java file, find the diff of the corresponding auto-merged file
        for (SourceFile manuallyMergedFile : manuallyMergedFiles) {
            String manualAbsolutePath = manuallyMergedFile.getAbsolutePath();
            String manualRelativePath = manuallyMergedFile.getRelativePath();
            String mergedAbsolutePath = mergedDir + "/" + manualRelativePath;
            double filePrecision = 0.0;
            double fileRecall = 0.0;

            // Get the number of manually merged lines of code
            int manualLOC = readFileToLines(manualAbsolutePath).size();
            // get the number of auto-merged lines of code
            int autoMergedLOC = computeFileLOC(mergedAbsolutePath);
            System.out.println("Manual LOC for " + manualAbsolutePath + ": " + manualLOC);
            System.out.println("Auto-merged LOC for " + mergedAbsolutePath + ": " + autoMergedLOC);
        }

            return null;
    }

    /*
     * Calculate the number of lines of code in the given file.
     */
    public static int computeFileLOC(String path) {
        List<String> lines =
                readFileToLines(path).stream().filter(line -> line.trim().length() > 0).collect(Collectors.toList());
        return lines.size();
    }


}
