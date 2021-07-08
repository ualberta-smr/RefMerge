package ca.ualberta.cs.smr.evaluation.data;

/*
 * Stores information about the conflict block.
 */
public class ConflictBlockData {
    private final String left;
    private final String right;
    private final int startLine;
    private final int endLine;

    public ConflictBlockData(String left, String right, int startLine, int endLine) {
        this.left = left;
        this.right = right;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getConflictingLOC() {
        return (endLine - startLine);
    }

}

