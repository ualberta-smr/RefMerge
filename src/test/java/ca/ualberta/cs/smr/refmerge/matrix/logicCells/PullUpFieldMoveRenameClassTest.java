package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PullUpFieldMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Pull up field A.foo -> B.foo
        PullUpFieldObject pullUpFieldObject = new PullUpFieldObject("A", "foo", "B", "foo");
        // Rename and move A0 -> A
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A0",
                "p", "A", "A", "p");

        // Rename B0 -> B
        MoveRenameClassObject moveRenameClassObject2 = new MoveRenameClassObject("B.java", "B0",
                "p", "B", "B", "p");
        PullUpFieldMoveRenameClassCell cell = new PullUpFieldMoveRenameClassCell(null);
        cell.checkCombination(moveRenameClassObject, pullUpFieldObject);

        // Should have changed A.foo -> B.foo to A0.foo -> B.foo
        Assert.assertEquals(pullUpFieldObject.getOriginalFilePath(), moveRenameClassObject.getOriginalFilePath());
        Assert.assertEquals(pullUpFieldObject.getOriginalClass(), moveRenameClassObject.getOriginalClassObject().getClassName());

        // Pull up field A.foo -> B.foo
        pullUpFieldObject = new PullUpFieldObject("A", "foo", "B", "foo");

        cell.checkCombination(moveRenameClassObject2, pullUpFieldObject);

        // Should have changed A.foo -> B.foo to A.foo -> B0.foo
        Assert.assertEquals(pullUpFieldObject.getDestinationFilePath(), moveRenameClassObject2.getOriginalFilePath());
        Assert.assertEquals(pullUpFieldObject.getTargetClass(), moveRenameClassObject2.getOriginalClassObject().getClassName());


    }
}
