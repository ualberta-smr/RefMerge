package ca.ualberta.cs.smr.evaluation.data;

import java.util.ArrayList;

public class ComparisonResult {

    private final int totalDiffFiles;
    private final int totalAutoMergedLOC;
    private final int totalManualMergedLOC;
    private final int totalSameLOCMerged;
    private final int totalSameLOCManual;
    private final double precision;
    private final double recall;
    private final ArrayList<FileDetails> files;

    public ComparisonResult(int totalDiffFiles, int totalAutoMergedLOC, int totalManualMergedLOC,
                            int totalSameLOCMerged, int totalSameLOCManual,
                            double precision, double recall, ArrayList<FileDetails> files) {
        this.totalDiffFiles = totalDiffFiles;
        this.totalAutoMergedLOC = totalAutoMergedLOC;
        this.totalManualMergedLOC = totalManualMergedLOC;
        this.totalSameLOCMerged = totalSameLOCMerged;
        this.totalSameLOCManual = totalSameLOCManual;
        this.precision = precision;
        this.recall = recall;
        this.files = files;
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

    public int getTotalAutoMergedLOC() {
        return totalAutoMergedLOC;
    }

    public int getTotalManualMergedLOC() {
        return totalManualMergedLOC;
    }

    public int getTotalSameLOCMerged() {
        return totalSameLOCMerged;
    }

    public int getTotalSameLOCManual() {
        return totalSameLOCManual;
    }

    public ArrayList<FileDetails> getFiles() {
        return files;
    }
}
