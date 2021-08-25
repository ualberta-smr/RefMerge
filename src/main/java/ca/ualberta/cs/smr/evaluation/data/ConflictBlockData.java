package ca.ualberta.cs.smr.evaluation.data;

/*
 * Stores information about the conflict block.
 */
public class ConflictBlockData {
    private final String left;
    private final String right;
    private final int startLine;
    private final int endLine;
    private final String path;
    private String mergeTool;
    private boolean isSame;
    private  boolean isComment;

    public ConflictBlockData(String left, String right, int startLine, int endLine, String path, String mergeTool, boolean isComment) {
        this.left = left;
        this.right = right;
        this.startLine = startLine;
        this.endLine = endLine;
        this.path = path;
        this.mergeTool = "";
        this.isSame = false;
        this.mergeTool = mergeTool;
        this.isComment = isComment;
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

    public String getPath() {
        return path;
    }


    public void setSame() {
        this.isSame = true;
    }

    public boolean isSame() {
        return isSame;
    }

    public String getMergeTool() {
        return mergeTool;
    }

    public boolean isComment() {
        return isComment;
    }

}

