package ca.ualberta.cs.smr.evaluation.data;

/*
 * Contains information about the java source file.
 */
public class SourceFile {
    private final String fileName;
    private final String relativePath;
    private final String absolutePath;

    public SourceFile(String fileName, String relativePath, String absolutePath) {
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
}
