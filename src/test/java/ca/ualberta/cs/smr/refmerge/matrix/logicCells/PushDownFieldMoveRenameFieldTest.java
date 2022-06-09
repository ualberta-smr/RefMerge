package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldMoveRenameFieldTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }


    public void testNamingConflict() {
        // Ref 1: A.foo moved and renamed to B.bar
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("A.java", "A", "foo", "B.java","B", "bar");
        // Ref 2: A.foo pushed down to C.foo (conflicts with ref 1)
        PushDownFieldObject pushDownFieldObject2 = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.bar pushed down to B.bar (conflicts with ref 1)
        PushDownFieldObject pushDownFieldObject3 = new PushDownFieldObject("A", "bar", "B", "bar");
        // Ref 3: B.bar pushed down to A.foo
        PushDownFieldObject pushDownFieldObject4 = new PushDownFieldObject("B", "bar", "A", "foo");


        PushDownFieldMoveRenameFieldCell cell = new PushDownFieldMoveRenameFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject4);
        Assert.assertFalse(isConflicting);
    }
}
