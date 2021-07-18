package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.evaluation.data.*;
import com.commentremover.app.CommentProcessor;
import com.commentremover.app.CommentRemover;
import com.commentremover.exception.CommentRemoverException;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import io.reflectoring.diffparser.api.DiffParser;
import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import io.reflectoring.diffparser.api.model.Line;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
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
    public static Pair<Pair<Integer, Integer>, List<Pair<ConflictingFileData, List<ConflictBlockData>>>> extractMergeConflicts(String directory) {
        ArrayList<SourceFile> temp = new ArrayList<>();
        File dir = new File(directory);
        ArrayList<SourceFile> mergedFiles = getJavaSourceFiles(directory, temp, dir);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> conflictingFiles = new ArrayList<>();
        int totalConflicts = 0;
        int totalConflictingLOC = 0;
        for(SourceFile file : mergedFiles) {
            int conflictingLOC = 0;
            List<ConflictBlockData> conflictBlocks = extractConflictBlocks(file.getAbsolutePath());
            // Get the total number of conflict blocks in the file
            totalConflicts += conflictBlocks.size();
            for(ConflictBlockData conflictBlock : conflictBlocks) {
                conflictingLOC += conflictBlock.getEndLine() - conflictBlock.getStartLine();
            }
            // If the file is conflicting
            if(conflictBlocks.size() > 0) {
                ConflictingFileData conflictingFileData = new ConflictingFileData(file.getAbsolutePath(), conflictBlocks.size(), conflictingLOC);
                conflictingFiles.add(Pair.of(conflictingFileData, conflictBlocks));
            }
            totalConflictingLOC += conflictingLOC;
        }

        return Pair.of(Pair.of(totalConflicts, totalConflictingLOC), conflictingFiles);
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
    private static List<ConflictBlockData> extractConflictBlocks(String path) {
        StringBuilder leftConflictingContent = new StringBuilder();
        StringBuilder rightConflictingContent = new StringBuilder();
        boolean inConflictBlock = false;
        boolean isLeftContent = false;
        int lineCounter = 0;
        int startLOC = 0;
        int endLOC = 0;

        List<ConflictBlockData> mergeConflicts = new ArrayList<>();
        List<String> lines = readFileToLines(path);
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            lineCounter++;
            if (line.contains(CONFLICT_LEFT_BEGIN)) {
                leftConflictingContent = new StringBuilder();
                rightConflictingContent = new StringBuilder();
                inConflictBlock = true;
                isLeftContent = true;
                startLOC = lineCounter;
                iterator.remove();
            } else if (line.contains(CONFLICT_RIGHT_BEGIN)) {
                isLeftContent = false;
                iterator.remove();
            } else if (line.contains(CONFLICT_RIGHT_END)) {
                endLOC = lineCounter;
                String leftContent = leftConflictingContent.toString();
                String rightContent = rightConflictingContent.toString();

                leftContent = leftContent.replaceAll(" ", "");
                leftContent = leftContent.replaceAll("\n", "");
                rightContent = rightContent.replaceAll(" ", "");
                rightContent = rightContent.replaceAll("\n", "");
                if(leftContent.length() == 0 && rightContent.length() == 0) {
                    continue;
                }
                ConflictBlockData mergeConflict = new ConflictBlockData(leftContent, rightContent, startLOC, endLOC);
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
    public static ComparisonResult compareAutoMerged(String mergedDir, List<SourceFile> manuallyMergedFiles,
                                                     String projectPath, boolean isReplication) {
        int numberOfDiffFiles = 0;
        double autoMergePrecision = 0.0;
        double autoMergeRecall = 0.0;
        int totalAutoMergedLOC = 0;
        int totalManualMergedLOC = 0;
        int totalSameLOCMerged = 0;
        int totalSameLOCManual = 0;

        // For each manually merged Java file, find the diff of the corresponding auto-merged file
        for (SourceFile manuallyMergedFile : manuallyMergedFiles) {
            String manualAbsolutePath = manuallyMergedFile.getAbsolutePath();
            String manualRelativePath = manuallyMergedFile.getRelativePath();
            String mergedAbsolutePath = mergedDir + "/" + manualRelativePath;

            // Get the number of manually merged lines of code
            int manualLOC = readFileToLines(manualAbsolutePath).size();
            // get the number of auto-merged lines of code
            int autoMergedLOC = readFileToLines(mergedAbsolutePath).size();

            totalManualMergedLOC += manualLOC;
            totalAutoMergedLOC += autoMergedLOC;

            int manualDiffLOC= 0;
            int mergedDiffLOC = 0;
            String diffOutput = GitUtils.diff(projectPath, manualAbsolutePath, mergedAbsolutePath);
            DiffParser parser = new UnifiedDiffParser();
            List<Diff> diffs = parser.parse(new ByteArrayInputStream(diffOutput.getBytes()));
            for(Diff diff : diffs) {
                List<Hunk> hunks = diff.getHunks();
                // If the files differ, add to the number of different files
                numberOfDiffFiles += hunks.size() > 0 ? 1 : 0;
                for(Hunk hunk : hunks) {
                    String manualContent = getHunkContent(hunk, Line.LineType.FROM);
                    String mergedContent = getHunkContent(hunk, Line.LineType.TO);
                    int manualHunkLOC = manualContent.length() > 0 ? hunk.getFromFileRange().getLineCount() : 0;
                    int mergedHunkLOC = mergedContent.length() > 0 ? hunk.getToFileRange().getLineCount() : 0;
                    manualDiffLOC += manualHunkLOC;
                    mergedDiffLOC += mergedHunkLOC;
                }
            }
            // If there are no autoMergedLOC, then there are no LOC that are the same. Additionally, if the diff is
            // greater than the autoMergedLOC, then there are no lines that are the same
            int sameLOCManual = 0;
            int sameLOCMerged = 0;
            if(autoMergedLOC > 0) {
                sameLOCManual = autoMergedLOC - manualDiffLOC;
            }
            if(autoMergedLOC > 0) {
                sameLOCMerged = autoMergedLOC - mergedDiffLOC;
            }
            totalSameLOCMerged += sameLOCMerged;
            totalSameLOCManual += sameLOCManual;

        }
        if(totalAutoMergedLOC > 0) {
            autoMergePrecision = totalSameLOCMerged / (double) totalAutoMergedLOC;
        }
        // If there are no auto-merged LOC, the precision is technically 1
        else {
            autoMergePrecision = 1.0;
        }
//        // The manually merged LOC should not be 0
        if(totalAutoMergedLOC > 0) {
            autoMergeRecall = totalSameLOCManual / (double) totalManualMergedLOC;
        }
        else {
            // If IntelliMerge replication, set recall to 1.0 because it was set to 1.0 in the IntelliMerge evaluation
            if(isReplication) {
                autoMergeRecall = 1.0;
            }
            // Otherwise set it to 0 because 0/x = 0
            else {
                autoMergeRecall = 0.0;
            }
        }

        return new ComparisonResult(numberOfDiffFiles, totalAutoMergedLOC, totalManualMergedLOC,
                totalSameLOCMerged, totalSameLOCManual, autoMergePrecision, autoMergeRecall);
    }

    /*
     * Format all java files with google-java-formatter in a directory
     */
    public static void formatAllJavaFiles(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    formatAllJavaFiles(f.getAbsolutePath());
                } else if (f.isFile() && isJavaFile(f)) {
                    String code = readFileToString(f);
                    try {
                        // Format with google-java-formatter
                        String reformattedCode = new Formatter().formatSource(code);
                        // Write the formatted string into the original file
                        writeContent(f.getAbsolutePath(), reformattedCode);
                    } catch (FormatterException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
     * Read the given file to a string
     */
    public static String readFileToString(File file) {
        String content = "";
        String fileEncoding = "UTF-8";
        try (BufferedReader reader = Files
                .newBufferedReader(Paths.get(file.getAbsolutePath()), Charset.forName(fileEncoding))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /*
     * Get the corresponding content from the hunk
     */
    private static String getHunkContent(
            Hunk hunk, Line.LineType lineType) {
        String content = hunk.getLines().stream().filter(line -> line.getLineType().equals(lineType)).map(Line::getContent)
                        .collect(Collectors.joining("\n"));

        return content.trim();
    }

    /*
     * Remove all comments in java files
     */
    public static void removeAllComments(String targetDir) {
        try {
            CommentRemover commentRemover =
                    new CommentRemover.CommentRemoverBuilder()
                            .removeJava(true)
                            .removeTodos(true) // Remove todos
                            .removeSingleLines(false) // Do not remove single line type comments
                            .removeMultiLines(true) // Remove multiple type comments
                            .preserveJavaClassHeaders(false) // Preserves class header comment
                            .preserveCopyRightHeaders(false) // Preserves copyright comment
                            .startExternalPath(targetDir) // Give it full path for external dir
                            .build();
            CommentProcessor commentProcessor = new CommentProcessor(commentRemover);
            commentProcessor.start();
        } catch (CommentRemoverException e) {
            e.printStackTrace();
        }
    }


}
