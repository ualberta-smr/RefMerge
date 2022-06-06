package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PullUpMethodPullUpMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpMethodObject pullUpMethodObject2 = new PullUpMethodObject("A", "foo", "C", "foo");
        // Ref 3: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpMethodObject pullUpMethodObject3 = new PullUpMethodObject("A", "foo", "C", "foo");

        PullUpMethodPullUpMethodCell cell = new PullUpMethodPullUpMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(pullUpMethodObject1, pullUpMethodObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpMethodObject2, pullUpMethodObject3);
        Assert.assertFalse(isConflicting);
    }

}
