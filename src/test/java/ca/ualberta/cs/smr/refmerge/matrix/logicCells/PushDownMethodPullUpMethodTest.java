package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownMethodPullUpMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pushed down to C.foo (Conflicts with Ref 1)
        PushDownMethodObject pushDownMethodObject2 = new PushDownMethodObject("A", "foo", "C", "foo");
        // Ref 3: C.foo pushed down to B.foo (Conflicts with Ref 1)
        PushDownMethodObject pushDownMethodObject3 = new PushDownMethodObject("C", "foo", "B", "foo");
        // Ref 4: A.bar pushed down to C.bar
        PushDownMethodObject pushDownMethodObject4 = new PushDownMethodObject("A", "bar", "C", "bar");
        // Ref 4: B.bar pushed down to C.bar
        PushDownMethodObject pushDownMethodObject5 = new PushDownMethodObject("B", "bar", "C", "bar");


        PushDownMethodPullUpMethodCell cell = new PushDownMethodPullUpMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(pullUpMethodObject1, pushDownMethodObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpMethodObject1, pushDownMethodObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpMethodObject1, pushDownMethodObject4);
        Assert.assertFalse(isConflicting);
        isConflicting = cell.conflictCell(pullUpMethodObject1, pushDownMethodObject5);
        Assert.assertFalse(isConflicting);
    }

}
