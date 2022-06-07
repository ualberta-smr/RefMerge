package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;

public class PullUpMethodExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: Extract B.foo from B.bar
        ExtractMethodObject extractMethodObject1 = new ExtractMethodObject("B.java", "B", bar,
                "B.java", "B", foo);
        // Ref 2: Extract B.bar from B.foo
        ExtractMethodObject extractMethodObject2 = new ExtractMethodObject("B.java", "B", foo,
                "B.java", "B", bar);
        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(extractMethodObject1, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(extractMethodObject2, pullUpMethodObject1);
        Assert.assertFalse(isConflicting);

    }

}
