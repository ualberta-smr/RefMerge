package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PullUpFieldMoveRenameFieldTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        // Ref 1: A.foo moved and renamed to B.bar
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("A.java", "A", "foo", "B.java","B", "bar");
        // Ref 2: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject2 = new PullUpFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.bar pulled up to B.bar (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject3 = new PullUpFieldObject("A", "bar", "B", "bar");
        // Ref 3: B.bar pulled up to A.foo
        PullUpFieldObject pullUpFieldObject4 = new PullUpFieldObject("B", "bar", "A", "foo");


        PullUpFieldMoveRenameFieldCell cell = new PullUpFieldMoveRenameFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject4);
        Assert.assertFalse(isConflicting);
    }
}
