package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.RefactoringType;

public class MoveRenameFieldMoveRenameFieldLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testRenameNamingConflict() {
        // A.foo -> A.bar
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.foo -> A.foobar (conflicts with 1)
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);
        // B.foo -> B.foobar (no conflict)
        MoveRenameFieldObject rightRenameFieldObject2 = new MoveRenameFieldObject("B.java", "B",
                "foo", "B.java", "B", "foobar");
        rightRenameFieldObject2.setType(RefactoringType.RENAME_ATTRIBUTE);
        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(getProject());
        boolean isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject2);
        Assert.assertFalse(isConflicting);
    }

    public void testMoveNamingConflict() {
        // A.foo -> B.foo
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "B.java", "B", "foo");
        leftRenameFieldObject.setType(RefactoringType.MOVE_ATTRIBUTE);
        // A.foo -> C.foo (conflicts with 1)
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "foo", "C.java", "C", "foo");
        rightRenameFieldObject1.setType(RefactoringType.MOVE_ATTRIBUTE);
        // A.foo -> B.foo (no conflict)
        MoveRenameFieldObject rightRenameFieldObject2 = new MoveRenameFieldObject("A.java", "A",
                "foo", "B.java", "B", "foo");
        rightRenameFieldObject2.setType(RefactoringType.MOVE_ATTRIBUTE);
        // C.foo -> B.foo (conflicts with 1)
        MoveRenameFieldObject rightRenameFieldObject3 = new MoveRenameFieldObject("C.java", "C",
                "foo", "B.java", "B", "foo");
        rightRenameFieldObject3.setType(RefactoringType.MOVE_ATTRIBUTE);
        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(getProject());
        boolean isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject2);
        Assert.assertFalse(isConflicting);
        isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject3);
        Assert.assertTrue(isConflicting);
    }

    public void testFoundTransitivity() {
        // A.foo -> A.bar
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.bar -> A.foobar
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "bar", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);

        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(getProject());
        boolean isTransitive = cell.checkTransitivity(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());
    }

    public void testFoundTransitivity2() {
        // A.foo -> A.bar
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.bar -> A.foobar
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "bar", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.foobar -> B.foobar
        MoveRenameFieldObject rightRenameFieldObject2 = new MoveRenameFieldObject("A.java", "A",
                "foobar", "B.java", "B", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.MOVE_ATTRIBUTE);
        // B.foobar -> C.barfoo
        MoveRenameFieldObject rightRenameFieldObject3 = new MoveRenameFieldObject("B.java", "B",
                "foobar", "C.java", "C", "barfoo");
        rightRenameFieldObject1.setType(RefactoringType.MOVE_RENAME_ATTRIBUTE);
        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(getProject());
        boolean isTransitive = cell.checkTransitivity(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());
        isTransitive = cell.checkTransitivity(rightRenameFieldObject1, rightRenameFieldObject2);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(rightRenameFieldObject1.getDestinationName(), rightRenameFieldObject2.getDestinationName());
        isTransitive = cell.checkTransitivity(rightRenameFieldObject2, rightRenameFieldObject3);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(rightRenameFieldObject2.getDestinationName(), rightRenameFieldObject3.getDestinationName());
        Assert.assertEquals(rightRenameFieldObject2.getDestinationClass(), rightRenameFieldObject3.getDestinationClass());
    }

    public void testNotFoundTransitivity() {
        // A.foo -> A.bar
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.foobar -> A.foobar
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "foobar", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);

        MoveRenameFieldMoveRenameFieldCell cell = new MoveRenameFieldMoveRenameFieldCell(getProject());
        boolean isTransitive = cell.checkTransitivity(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertFalse(isTransitive);
        Assert.assertNotEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());
    }
}
