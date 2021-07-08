package ca.ualberta.cs.smr.evaluation.data;

public class FileDetails {
    private String relativePath;
    private int manualLOC;
    private int autoMergedLOC;
    private int correctLOCInAutoMerged;
    private int correctLOCInManual;
    private double precision;
    private double recall;

    public FileDetails(String relativePath, int manualLOC, int autoMergedLOC, int correctLOCInAutoMerged, int correctLOCInManual,
                       double precision, double recall) {
        this.relativePath = relativePath;
        this.manualLOC = manualLOC;
        this.autoMergedLOC = autoMergedLOC;
        this.correctLOCInAutoMerged = correctLOCInAutoMerged;
        this.correctLOCInManual = correctLOCInManual;
        this.precision = precision;
        this.recall = recall;
    }
}
