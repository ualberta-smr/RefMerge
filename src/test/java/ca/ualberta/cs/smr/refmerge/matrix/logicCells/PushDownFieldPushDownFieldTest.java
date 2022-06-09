package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldPushDownFieldTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Ref 1: Field A.foo pushed down to B.foo
        PushDownFieldObject pushDownMethodObject1 = new PushDownFieldObject("A", "foo", "B", "foo");
        // Ref 2: Field A.foo pushed down to C.foo
        PushDownFieldObject pushDownMethodObject2 = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 3: Field D.foo pushed down to C.foo (conflicts with ref 2)
        PushDownFieldObject pushDownMethodObject3 = new PushDownFieldObject("D", "foo", "C", "foo");

        PushDownFieldPushDownFieldCell cell = new PushDownFieldPushDownFieldCell(getProject());

        boolean isConflicting = cell.namingConflict(pushDownMethodObject2, pushDownMethodObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pushDownMethodObject1, pushDownMethodObject2);
        Assert.assertFalse(isConflicting);
    }
}
