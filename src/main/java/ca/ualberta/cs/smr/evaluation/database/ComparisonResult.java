package ca.ualberta.cs.smr.evaluation.database;

public class ComparisonResult {

    private final int totalDiffFiles;
    private int totalAutoMergedLOC;
    private int totalManualMergedLOC;
    private int totalSameLOCMerged;
    private int totalSameLOCManual;
    private final double precision;
    private final double recall;

    public ComparisonResult(int totalDiffFiles, int totalAutoMergedLOC, int totalManualMergedLOC,
                            int totalSameLOCMerged, int totalSameLOCManual,
                            double precision, double recall) {
        this.totalDiffFiles = totalDiffFiles;
        this.totalAutoMergedLOC = totalAutoMergedLOC;
        this.totalManualMergedLOC = totalManualMergedLOC;
        this.totalSameLOCMerged = totalSameLOCMerged;
        this.totalSameLOCManual = totalSameLOCManual;
        this.precision = precision;
        this.recall = recall;
    }

    public int getTotalDiffFiles() {
        return totalDiffFiles;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }
}
