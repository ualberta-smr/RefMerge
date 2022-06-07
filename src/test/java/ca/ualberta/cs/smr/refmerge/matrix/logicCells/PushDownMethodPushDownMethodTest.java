package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownMethodPushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Ref 1: A.foo pushed down to B.foo
        PushDownMethodObject pushDownMethodObject1 = new PushDownMethodObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pushed down to C.foo
        PushDownMethodObject pushDownMethodObject2 = new PushDownMethodObject("A", "foo", "C", "foo");
        // Ref 3: D.foo pushed down to C.foo (conflicts with ref 2)
        PushDownMethodObject pushDownMethodObject3 = new PushDownMethodObject("D", "foo", "C", "foo");

        PushDownMethodPushDownMethodCell cell = new PushDownMethodPushDownMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(pushDownMethodObject2, pushDownMethodObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pushDownMethodObject1, pushDownMethodObject2);
        Assert.assertFalse(isConflicting);
    }

    public void testCheckTransitivity() {
        // Ref 1: A.foo pulled up to B.foo
        PushDownMethodObject pushDownMethodObject1 = new PushDownMethodObject("A", "foo", "C", "foo");
        // Ref 2: C.foo pulled up to B.foo
        PushDownMethodObject pushDownMethodObject2 = new PushDownMethodObject("A", "foo", "B", "foo");
        pushDownMethodObject2.addSubClass("D", "D");
        pushDownMethodObject1.addSubClass("D", "D");

        PushDownMethodPushDownMethodCell cell = new PushDownMethodPushDownMethodCell(getProject());

        boolean isTransitive= cell.checkTransitivity(pushDownMethodObject1, pushDownMethodObject2);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(pushDownMethodObject1.getSubClasses().size(), 3);
        Assert.assertEquals(pushDownMethodObject2.getSubClasses().size(), 3);
    }

}
