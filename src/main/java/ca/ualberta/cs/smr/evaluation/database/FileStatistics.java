package ca.ualberta.cs.smr.evaluation.database;

import ca.ualberta.cs.smr.evaluation.data.FileDetails;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("file_statistics")
public class FileStatistics extends Model {

    public FileStatistics(MergeResult mergeResult, FileDetails fileDetails) {
        set("merge_tool", mergeResult.getMergeTool(), "path", fileDetails.getPath(),
                "auto_merged_loc", fileDetails.getAutoMergedLOC(),
                "manual_merged_loc", fileDetails.getManualLOC(),
                "same_auto_merged_loc", fileDetails.getSameAutoMergedLOC(),
                "same_manual_loc", fileDetails.getSameManualLOC(),
                "file_precision", fileDetails.getPrecision(),
                "file_recall", fileDetails.getRecall(),
                "merge_result_id", mergeResult.getId(),
                "merge_commit_id", mergeResult.getMergeCommitId(),
                "project_id", mergeResult.getProjectId());
    }
}
