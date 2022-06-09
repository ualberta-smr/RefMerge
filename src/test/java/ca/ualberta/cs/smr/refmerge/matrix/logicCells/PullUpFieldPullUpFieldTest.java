package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PullUpFieldPullUpFieldTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Ref 1: A.foo pulled up to B.foo
        PullUpFieldObject pullUpFieldObject1 = new PullUpFieldObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject2 = new PullUpFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject3 = new PullUpFieldObject("A", "foo", "C", "foo");

        PullUpFieldPullUpFieldCell cell = new PullUpFieldPullUpFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(pullUpFieldObject1, pullUpFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpFieldObject2, pullUpFieldObject3);
        Assert.assertFalse(isConflicting);
    }
}
