package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenameFieldMoveRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckCombination() {
        MoveRenameClassObject classObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        RenameFieldObject fieldObject = new RenameFieldObject("A.java", "A", "foo",
                "A.java", "A", "bar");
        RenameFieldObject fieldObject2 = new RenameFieldObject("B.java", "B", "foo",
                "B.java", "B", "bar");

        RenameFieldMoveRenameClassCell.checkCombination(classObject, fieldObject);
        RenameFieldMoveRenameClassCell.checkCombination(classObject, fieldObject2);
        Assert.assertEquals(fieldObject.getOriginalClass(), fieldObject2.getOriginalClass());
        Assert.assertEquals(fieldObject.getDestinationClass(), fieldObject2.getDestinationClass());


    }

}
