package ca.ualberta.cs.smr.evaluation.model;

/*
 * Stores information about the conflict block.
 */
public class ConflictBlock {
    private String left;
    private String right;
    private int startLine;
    private int endLine;

    public ConflictBlock(String left, String right, int startLine, int endLine) {
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


}

