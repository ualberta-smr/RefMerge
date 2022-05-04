package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class MoveRenameFieldMoveRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckCombination() {
        MoveRenameClassObject classObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        MoveRenameFieldObject fieldObject = new MoveRenameFieldObject("A.java", "A", "foo",
                "A.java", "A", "bar");
        MoveRenameFieldObject fieldObject2 = new MoveRenameFieldObject("B.java", "B", "foo",
                "B.java", "B", "bar");

        MoveRenameFieldMoveRenameClassCell.checkCombination(classObject, fieldObject);
        MoveRenameFieldMoveRenameClassCell.checkCombination(classObject, fieldObject2);
        Assert.assertEquals(fieldObject.getOriginalClass(), fieldObject2.getOriginalClass());
        Assert.assertEquals(fieldObject.getDestinationClass(), fieldObject2.getDestinationClass());

    }

    public void testCheckCombination2() {

        MoveRenameClassObject classObject2 = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        MoveRenameFieldObject fieldObject = new MoveRenameFieldObject("A.java", "A", "foo",
                "A.java", "A", "bar");

        MoveRenameFieldMoveRenameClassCell.checkCombination(classObject2, fieldObject);
        Assert.assertEquals(classObject2.getOriginalClassObject().getClassName(), fieldObject.getOriginalClass());
        Assert.assertEquals(classObject2.getDestinationClassObject().getClassName(), fieldObject.getDestinationClass());


    }

}
