package ca.ualberta.cs.smr.evaluation.database;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("conflict_block")
public class ConflictBlock extends Model {

    public ConflictBlock() {}

    public ConflictBlock(ConflictingFile conflictingFile, ConflictBlockData conflictBlockData) {
        set("path", conflictingFile.getPath(), "conflicting_loc", conflictBlockData.getConflictingLOC(),
                "start_line", conflictBlockData.getStartLine(), "end_line", conflictBlockData.getEndLine(),
                "conflicting_file_id", conflictingFile.getId(), "merge_result_id", conflictingFile.getMergeResultId(),
                "merge_commit_id", conflictingFile.getMergeCommitId(), "project_id", conflictingFile.getProjectId());
    }

}
