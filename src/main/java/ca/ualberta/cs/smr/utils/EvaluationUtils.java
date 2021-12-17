package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.evaluation.data.*;
import com.commentremover.app.CommentProcessor;
import com.commentremover.app.CommentRemover;
import com.commentremover.exception.CommentRemoverException;
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

import static edu.pku.intellimerge.util.Utils.flattenString;

/*
 * Contains the methods necessary to get the metrics for the evaluation.
 */
public class EvaluationUtils {

    public static final String CONFLICT_LEFT_BEGIN = "<<<<<<<";
    static final String CONFLICT_BASE_BEGIN = "|||||||";
    public static final String CONFLICT_RIGHT_BEGIN = "=======";
    public static final String CONFLICT_RIGHT_END = ">>>>>>>";


    /*
     * Get the number of merge conflicts as well as each conflicting file.
     */
    public static List<Pair<ConflictingFileData, List<ConflictBlockData>>>
    extractMergeConflicts(String directory, String mergeTool, boolean isDiff2) {
        ArrayList<SourceFile> temp = new ArrayList<>();
        File dir = new File(directory);
        ArrayList<SourceFile> mergedFiles = getJavaSourceFiles(directory, temp, dir);
        List<Pair<ConflictingFileData, List<ConflictBlockData>>> conflictingFiles = new ArrayList<>();
        for(SourceFile file : mergedFiles) {
            int conflictingLOC = 0;
            List<ConflictBlockData> conflictBlocks = extractConflictBlocks(file.getAbsolutePath(), mergeTool, isDiff2);
            for(ConflictBlockData conflictBlock : conflictBlocks) {
                conflictingLOC += conflictBlock.getEndLine() - conflictBlock.getStartLine();
            }
            // If the file is conflicting
            if(conflictBlocks.size() > 0) {
                ConflictingFileData conflictingFileData = new ConflictingFileData(file.getRelativePath(), conflictBlocks.size(), conflictingLOC);
                conflictingFiles.add(Pair.of(conflictingFileData, conflictBlocks));
            }
        }

        return conflictingFiles;
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
        return file.getName().toLowerCase().endsWith(".java");
    }


    public static List<ConflictBlockData> extractConflictBlocks(String path, String mergeTool, boolean isDiff2) {
        if(isDiff2) {
            return extractConflictBlocksDiff2(path, mergeTool);
        }
        else {
            return extractConflictBlocksDiff3(path, mergeTool);

        }
    }

    /*
     * Extract merge conflicts from the file using diff3 for replication
     *
     * @param path
     * @param removeConflicts whether to remove conflict blocks while extracting
     * @return list of merge conflicts
     */
    public static List<ConflictBlockData> extractConflictBlocksDiff3(String path, String mergeTool) {
        // diff3 conflict style
        StringBuilder leftConflictingContent = new StringBuilder();
        StringBuilder rightConflictingContent = new StringBuilder();
        StringBuilder baseConflictingContent = new StringBuilder();
        boolean isConflictBlock = false;
        boolean isBaseContent = false;
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
                isConflictBlock = true;
                isLeftContent = true;
                leftConflictingContent = new StringBuilder();
                rightConflictingContent = new StringBuilder();
                baseConflictingContent = new StringBuilder();
                startLOC = lineCounter;
                iterator.remove();

            } else if (line.contains(CONFLICT_BASE_BEGIN)) {
                isLeftContent = false;
                isBaseContent = true;
                iterator.remove();

            } else if (line.contains(CONFLICT_RIGHT_BEGIN)) {
                isBaseContent = false;
                iterator.remove();

            } else if (line.contains(CONFLICT_RIGHT_END)) {
                endLOC = lineCounter;

                // reset the flags
                isConflictBlock = false;
                isBaseContent = false;
                isLeftContent = false;
                String leftContent = leftConflictingContent.toString();
                String rightContent = rightConflictingContent.toString();
                leftContent = flattenString(leftContent).trim();
                rightContent = flattenString(rightContent).trim();

                boolean isSingleLine = false;
                if ((endLOC - startLOC) == 3) {
                    isSingleLine = true;
                }
                boolean isComment = false;
                if (leftContent.length() == 0 && (isComment(rightContent, isSingleLine) || (isAnnotation(rightContent) && rightContent.length() < 30))) {
                    isComment = true;
                }
                else if (rightContent.length() == 0 && (isComment(leftContent, isSingleLine) || (isAnnotation(leftContent) && leftContent.length() < 30))) {
                    isComment = true;

                }
                else if (isComment(leftContent, isSingleLine) && isComment(rightContent, isSingleLine)) {
                    isComment = true;
                }

                ConflictBlockData mergeConflict = new ConflictBlockData(leftContent, rightContent, startLOC, endLOC, path, mergeTool, isComment);
                mergeConflicts.add(mergeConflict);
                iterator.remove();

            } else {
                if (isConflictBlock) {
                    if (isLeftContent) {
                        leftConflictingContent.append(line).append("\n");
                    } else if(isBaseContent) {
                        baseConflictingContent.append(line).append("\n");
                    }
                    else {
                        rightConflictingContent.append(line).append("\n");
                    }
                    iterator.remove();
                }
            }
        }
        if (mergeConflicts.size() > 0) {
            writeLinesToFile(path, lines);
        }
        return mergeConflicts;
    }

    /*
     * Extract the individual merge conflicts from the file and record their information.
     */
    private static List<ConflictBlockData> extractConflictBlocksDiff2(String path, String mergeTool) {
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
            } else if (line.contains(CONFLICT_RIGHT_END) && inConflictBlock) {
                inConflictBlock = false;
                isLeftContent = false;
                endLOC = lineCounter;
                String leftContent = leftConflictingContent.toString();
                String rightContent = rightConflictingContent.toString();

                leftContent = flattenString(leftContent).trim();
                rightContent = flattenString(rightContent).trim();

//                if(leftContent.length() == 0 && rightContent.length() == 0) {
//                    iterator.remove();
//                    continue;
//                }
//                if(leftContent.equals(rightContent)) {
//                    iterator.remove();
//                    continue;
//                }
                boolean isSingleLine = false;
                if ((endLOC - startLOC) == 3) {
                    isSingleLine = true;
                }
                boolean isComment = false;
                if (leftContent.length() == 0 && (isComment(rightContent, isSingleLine) || (isAnnotation(rightContent) && rightContent.length() < 30))) {
                    isComment = true;
                }
                else if (rightContent.length() == 0 && (isComment(leftContent, isSingleLine) || (isAnnotation(leftContent) && leftContent.length() < 30))) {
                    isComment = true;

                }
                else if (isComment(leftContent, isSingleLine) && isComment(rightContent, isSingleLine)) {
                    isComment = true;
                }


                ConflictBlockData mergeConflict = new ConflictBlockData(leftContent, rightContent, startLOC, endLOC, path, mergeTool, isComment);
                mergeConflicts.add(mergeConflict);
                // reset the flags
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
     * Check if the conflicting code is a comment
     */
    private static boolean isComment(String content, boolean isSingleLine) {
        if(content.startsWith("/*") && content.endsWith("*/")) {
            return true;
        }
        else if(content.startsWith("//") && isSingleLine) {
            return true;
        }
        return false;
    }

    /*
     * Check if the conflicting code is an annotation
     */
    private static boolean isAnnotation(String content) {
        if(content.contains("@")) {
            return true;
        }
        return false;
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
                                                     String projectPath, List<String> relativePaths, boolean isReplication) {

        // If Git does not have any conflicts, do not calculate precision/recall.
        ArrayList<FileDetails> files = new ArrayList<>();

        if(relativePaths.size() == 0) {
            return new ComparisonResult(0, -1, -1,
                    -1, -1, -1, -1, null);
        }

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

            if (!relativePaths.contains(manualRelativePath)) {
                continue;
            }

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

            int diffHunks = 0;
            for(Diff diff : diffs) {
                List<Hunk> hunks = diff.getHunks();

                List<Hunk> visitedHunks = new ArrayList<>();
                for(Hunk hunk : hunks) {
                    if(isReplication) {
                        if(!removeMovingCausedHunks(hunk, visitedHunks)) {
                            visitedHunks.add(hunk);
                        }
                    }
                    else {
                        visitedHunks.add(hunk);
                    }
                }
                removeFormatCausedHunks(visitedHunks);
                // If the files differ, add to the number of different files
                numberOfDiffFiles += hunks.size() > 0 ? 1 : 0;
                for(Hunk hunk : visitedHunks) {
                    String manualContent = getHunkContent(hunk, Line.LineType.FROM, true);
                    String mergedContent = getHunkContent(hunk, Line.LineType.TO, true);

                    int manualHunkLOC = manualContent.length() > 0 ? hunk.getFromFileRange().getLineCount() : 0;
                    int mergedHunkLOC = mergedContent.length() > 0 ? hunk.getToFileRange().getLineCount() : 0;
                    manualDiffLOC += manualHunkLOC;
                    mergedDiffLOC += mergedHunkLOC;
                }
                diffHunks += visitedHunks.size();
            }

            // If in replication mode and there are no different hunks detected, assume that the sameloc is the same as total
            // for the given file.
            if(isReplication && diffHunks == 0) {
                totalSameLOCMerged += autoMergedLOC;
                totalSameLOCManual += manualLOC;
            }
            else {
                // If there are no autoMergedLOC, then there are no LOC that are the same. Additionally, if the diff is
                // greater than the autoMergedLOC, then there are no lines that are the same
                int sameLOCManual = 0;
                int sameLOCMerged = 0;
                double filePrecision = 0.0;
                double fileRecall = 0.0;
                if (autoMergedLOC > 0) {
                    sameLOCManual = manualLOC - manualDiffLOC;
                }
                if (autoMergedLOC > 0) {
                    sameLOCMerged = autoMergedLOC - mergedDiffLOC;
                }
                totalSameLOCMerged += sameLOCMerged;
                totalSameLOCManual += sameLOCManual;

                if(autoMergedLOC > 0) {
                    filePrecision = sameLOCMerged / (double) autoMergedLOC;
                }
                else {
                    filePrecision = 0.0;
                }
                if(manualLOC > 0) {
                    fileRecall = sameLOCManual / (double) manualLOC;
                }
                else {
                    fileRecall = 0.0;
                }

                FileDetails fileDetails = new FileDetails(manualRelativePath, manualLOC, autoMergedLOC,
                        sameLOCMerged, sameLOCManual, filePrecision, fileRecall);
                files.add(fileDetails);
            }

        }
        if(totalAutoMergedLOC > 0) {
            autoMergePrecision = totalSameLOCMerged / (double) totalAutoMergedLOC;
        }
        // If there are no auto-merged LOC, the precision is technically 1
        else {
            autoMergePrecision = 1.0;
        }
        // The manually merged LOC should not be 0
        if(totalAutoMergedLOC > 0 && totalManualMergedLOC > 0) {
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
                totalSameLOCMerged, totalSameLOCManual, autoMergePrecision, autoMergeRecall, files);
    }

    /*
     * Remove diff hunks that have the identical content ignoring empty chars
     * Specifically for replication
     * @param hunks
     */
    private static void removeFormatCausedHunks(List<Hunk> hunks) {
        List<Hunk> hunksCopy = new ArrayList<>(hunks);
        for (Hunk hunk : hunksCopy) {
            String hunkFromContent = getHunkContent(hunk, Line.LineType.FROM, true);
            String hunkToContent = getHunkContent(hunk, Line.LineType.TO, true);
            if (hunkFromContent.equals(hunkToContent)) {
                hunks.remove(hunk);
            }
        }
    }

    /*
     * Get the corresponding content from the hunk. Ignore empty chars if performing replication
     */
    private static String getHunkContent(Hunk hunk, Line.LineType lineType, boolean ignoreEmptyChars) {
        String content = hunk.getLines().stream().filter(line -> line.getLineType().equals(lineType)).map(Line::getContent)
                .collect(Collectors.joining("\n"));

        return ignoreEmptyChars ? flattenString(content).trim() : content.trim();
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
     * Remove all comments in java files
     */
    public static void removeAllComments(String targetDir) {
        try {
            CommentRemover commentRemover =
                    new CommentRemover.CommentRemoverBuilder()
                            .removeJava(true)
                            .removeTodos(true) // Remove todos
                            .removeSingleLines(true) // Do not remove single line type comments
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

    private static boolean removeMovingCausedHunks(Hunk hunk, List<Hunk> visitedHunks) {
        for (Hunk visitedHunk : visitedHunks) {
            // P.S. since actually \n brings deviation to line ranges, so here we directly compare hunk
            // contents
            // check if line ranges are opposite, e.g. @@ -130,47 +132,0 @@ and @@ -330,0 +287,47 @@
            //      if (visitedHunk.getFromFileRange().getLineCount() ==
            // hunk.getToFileRange().getLineCount()
            //          && visitedHunk.getToFileRange().getLineCount()
            //              == hunk.getFromFileRange().getLineCount()) {
            // check if hunk contents are the same
            String hunkFromContent = getHunkContent(hunk, Line.LineType.FROM, true);
            String visitedHunkFromContent = getHunkContent(visitedHunk, Line.LineType.FROM, true);
            String hunkToContent = getHunkContent(hunk, Line.LineType.TO, true);
            String visitedHunkToContent = getHunkContent(visitedHunk, Line.LineType.TO, true);
            if (hunkFromContent.equals(visitedHunkToContent)
                    && hunkToContent.equals(visitedHunkFromContent)) {

                visitedHunks.remove(visitedHunk);
                return true;
            }
            //      }
        }
        return false;
    }

    /*
     * Compare the conflicts reported by RefMerge and IntelliMerge to check for discrepancies. Add discrepancies to
     * a new list.
     */
    public static void getSameConflicts(
            List<Pair<ConflictingFileData, List<ConflictBlockData>>> refMergeConflicts,
            List<Pair<ConflictingFileData, List<ConflictBlockData>>> intelliMergeConflicts) {

        for(Pair<ConflictingFileData, List<ConflictBlockData>> refMergePairs : refMergeConflicts) {
            String refMergeFile = refMergePairs.getLeft().getFilePath();
            for(Pair<ConflictingFileData, List<ConflictBlockData>> intelliMergePairs : intelliMergeConflicts) {
                String intelliMergeFile = intelliMergePairs.getLeft().getFilePath();
                // Compare the files, if the files are different, then the conflicts can't be the same.
                if(!refMergeFile.equals(intelliMergeFile)) {
                    break;
                }
                // Compare each RefMerge conflict block with each IntelliMerge conflict block within the same file.
                for(ConflictBlockData refMergeConflictBlock : refMergePairs.getRight()) {
                    String refMergeLeftContent = refMergeConflictBlock.getLeft();
                    String refMergeRightContent = refMergeConflictBlock.getRight();

                    for(ConflictBlockData intelliMergeConflictBlock : intelliMergePairs.getRight()) {
                        String intelliMergeLeftContent = intelliMergeConflictBlock.getLeft();
                        String intelliMergeRightContent = intelliMergeConflictBlock.getRight();

                        if(refMergeLeftContent.equals(intelliMergeLeftContent)
                                && refMergeRightContent.equals(intelliMergeRightContent)) {
                            refMergeConflictBlock.setSame();
                            intelliMergeConflictBlock.setSame();
                        }
                    }
                }
            }
        }
    }

    /*
     * Remove all non-java files to save space.
     */
    public static void removeUnmergedAndNonJavaFiles(String path) {
        removeNonJavaFiles(path);
    }

    /*
     * Delete all files within the merged results that are not java files.
     */
    public static void removeNonJavaFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    removeNonJavaFiles(f.getAbsolutePath());
                } else if (f.isFile() && !isJavaFile(f)) {
                    if(!f.getName().startsWith(".")) {
                        boolean isDeleted = f.delete();
                        if (!isDeleted) {
                            System.out.println("Cannot delete " + f.getName());
                        }
                    }
                }
            }
        } else {
            System.out.println(path + " does not exist!");
        }

    }

}
