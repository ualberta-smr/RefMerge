package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.RefactoringType;

public class RenameFieldRenameFieldLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
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
        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(getProject());
        boolean isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject2);
        Assert.assertFalse(isConflicting);
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

        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(getProject());
        boolean isTransitive = cell.checkTransitivity(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());
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

        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(getProject());
        boolean isTransitive = cell.checkTransitivity(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertFalse(isTransitive);
        Assert.assertNotEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());
    }
}
