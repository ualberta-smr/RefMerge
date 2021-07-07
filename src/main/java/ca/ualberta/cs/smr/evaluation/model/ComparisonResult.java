package ca.ualberta.cs.smr.evaluation.model;

public class ComparisonResult {

    private int totalAutoMergedLOC;
    private int totalManualMergedLOC;
    private int totalSameLOCMerged;
    private int totalSameLOCManual;
    private double precision;
    private double recall;

    public ComparisonResult(int totalAutoMergedLOC, int totalManualMergedLOC,
                            int totalSameLOCMerged, int totalSameLOCManual,
                            double precision, double recall) {
        this.totalAutoMergedLOC = totalAutoMergedLOC;
        this.totalManualMergedLOC = totalManualMergedLOC;
        this.totalSameLOCMerged = totalSameLOCMerged;
        this.totalSameLOCManual = totalSameLOCManual;
        this.precision = precision;
        this.recall = recall;
    }
}
