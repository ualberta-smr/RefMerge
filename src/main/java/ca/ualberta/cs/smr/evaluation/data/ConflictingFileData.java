package ca.ualberta.cs.smr.evaluation.data;


public class ConflictingFileData {

    private String path;
    private int conflictingBlocks;
    private int conflictingLOC;

    public ConflictingFileData(String filePath, int conflictingBlocks, int conflictingLOC) {
        this.path = filePath;
        this.conflictingBlocks = conflictingBlocks;
        this.conflictingLOC = conflictingLOC;
    }

    public String getFilePath() {
        return path;
    }

    public int getConflictingBlocks() {
        return conflictingBlocks;
    }

    public int getConflictingLOC() {
        return conflictingLOC;
    }
}
