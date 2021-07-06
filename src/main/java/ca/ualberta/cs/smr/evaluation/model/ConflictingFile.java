package ca.ualberta.cs.smr.evaluation.model;

public class ConflictingFile {
    private String filePath;
    private int conflictingBlocks;
    private int conflictingLOC;

    public ConflictingFile(String filePath, int conflictingBlocks, int conflictingLOC) {
        this.filePath = filePath;
        this.conflictingBlocks = conflictingBlocks;
        this.conflictingLOC = conflictingLOC;
    }
}
