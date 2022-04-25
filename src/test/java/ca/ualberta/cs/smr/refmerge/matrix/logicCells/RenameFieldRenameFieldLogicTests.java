package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameFieldObject;
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
        RenameFieldObject leftRenameFieldObject = new RenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.foo -> A.foobar (conflicts with 1)
        RenameFieldObject rightRenameFieldObject1 = new RenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);
        // B.foo -> B.foobar (no conflict)
        RenameFieldObject rightRenameFieldObject2 = new RenameFieldObject("B.java", "B",
                "foo", "B.java", "B", "foobar");
        rightRenameFieldObject2.setType(RefactoringType.RENAME_ATTRIBUTE);
        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(getProject());
        boolean isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkFieldNamingConflict(leftRenameFieldObject, rightRenameFieldObject2);
        Assert.assertFalse(isConflicting);
    }
}
