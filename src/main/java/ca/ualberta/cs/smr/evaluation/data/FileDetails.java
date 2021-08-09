package ca.ualberta.cs.smr.evaluation.data;

public class FileDetails {
    private final String relativePath;
    private final int manualLOC;
    private final int autoMergedLOC;
    private final int correctLOCInAutoMerged;
    private final int correctLOCInManual;
    private final double precision;
    private final double recall;

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

    public String getPath() {
        return relativePath;
    }

    public int getManualLOC() {
        return manualLOC;
    }

    public int getAutoMergedLOC() {
        return autoMergedLOC;
    }

    public int getSameAutoMergedLOC() {
        return correctLOCInAutoMerged;
    }

    public int getSameManualLOC() {
        return correctLOCInManual;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }
}
