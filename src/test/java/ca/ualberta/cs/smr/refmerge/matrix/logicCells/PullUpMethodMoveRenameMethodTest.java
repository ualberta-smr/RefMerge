package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;

public class PullUpMethodMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: Rename A.foo to A.bar
        MoveRenameMethodObject moveRenameMethodObject1 = new MoveRenameMethodObject("",
                "A", foo, "", "A", bar);
        // Ref 3: Move C.foo to B.foo
        MoveRenameMethodObject moveRenameMethodObject2 = new MoveRenameMethodObject("",
                "C", foo, "", "B", foo);
        // Ref 3: Move C.foo to D.foo
        MoveRenameMethodObject moveRenameMethodObject3 = new MoveRenameMethodObject("",
                "C", foo, "", "D", foo);
        PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameMethodObject1, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject2, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject3, pullUpMethodObject1);
        Assert.assertFalse(isConflicting);
    }

}
