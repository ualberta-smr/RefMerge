package ca.ualberta.cs.smr.evaluation.database;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("merge_commit")
public class MergeCommit extends Model {

    public MergeCommit() {
    }

    public MergeCommit(String commitHash, boolean isConflicting, String parent1, String parent2, Project project, long timestamp) {
        set("commit_hash", commitHash, "project_id", project.getId(), "is_conflicting", isConflicting, "parent_1", parent1,
                "parent_2", parent2, "timestamp", timestamp);
    }

    public int getProjectId() {
        return getInteger("project_id");
    }

    public  boolean isDone() {
        return getBoolean("is_done");
    }

    public void setDone() {
        setBoolean("is_done", true);
    }

}
