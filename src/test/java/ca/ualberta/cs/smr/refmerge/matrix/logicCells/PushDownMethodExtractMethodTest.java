package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;

public class PushDownMethodExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pushed down to B.foo
        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");
        // Ref 2: Extract B.foo from B.bar
        ExtractMethodObject extractMethodObject1 = new ExtractMethodObject("B.java", "B", bar,
                "B.java", "B", foo);
        // Ref 2: Extract B.bar from B.foo
        ExtractMethodObject extractMethodObject2 = new ExtractMethodObject("B.java", "B", foo,
                "B.java", "B", bar);
        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(extractMethodObject1, pushDownMethodObject);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(extractMethodObject2, pushDownMethodObject);
        Assert.assertFalse(isConflicting);

    }
}
