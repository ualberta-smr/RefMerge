package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;

public class PushDownMethodInlineMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pushed down to B.foo
        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");
        // Ref 2: Inline A.foo to A.bar
        InlineMethodObject inlineMethodObject1 =
                new InlineMethodObject("A.java", "A", foo, "A.java", "A", bar);

        PushDownMethodInlineMethodCell cell = new PushDownMethodInlineMethodCell(getProject());
        boolean isConflicting = cell.conflictCell(inlineMethodObject1, pushDownMethodObject);
        Assert.assertTrue(isConflicting);

    }
}
