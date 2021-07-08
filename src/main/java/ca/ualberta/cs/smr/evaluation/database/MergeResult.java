package ca.ualberta.cs.smr.evaluation.database;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("merge_result")
public class MergeResult extends Model {


    public MergeResult() {
    }

    public MergeResult(String mergeTool, int totalConflicts, int totalDiffFiles, double precision, double recall,
                       long runtime, MergeCommit mergeCommit) {
        set("merge_tool", mergeTool,
                "total_conflicts", totalConflicts,
                "total_diff_files", totalDiffFiles,
                "auto_merged_precision", precision,
                "auto_merged_recall", recall,
                "runtime", runtime,
                "merge_commit_id", mergeCommit.getId(),
                "project_id", mergeCommit.getProjectId());
    }

    public String getMergeTool() {
        return getString("merge_tool");
    }

    public int getMergeCommitId() {
        return getInteger("merge_commit_id");
    }

    public int getProjectId() {
        return getInteger("project_id");
    }
}
