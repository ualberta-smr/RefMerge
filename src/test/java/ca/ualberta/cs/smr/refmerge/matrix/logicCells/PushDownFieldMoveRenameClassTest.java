package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Push down field A.foo -> B.foo
        PushDownFieldObject pushDownFieldObject = new PushDownFieldObject("A", "foo", "B", "foo");
        // Rename and move A0 -> A
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A0",
                "p", "A", "A", "p");

        // Rename B0 -> B
        MoveRenameClassObject moveRenameClassObject2 = new MoveRenameClassObject("B.java", "B0",
                "p", "B", "B", "p");
        PushDownFieldMoveRenameClassCell cell = new PushDownFieldMoveRenameClassCell(null);
        cell.checkCombination(moveRenameClassObject, pushDownFieldObject);

        // Should have changed A.foo -> B.foo to A0.foo -> B.foo
        Assert.assertEquals(pushDownFieldObject.getOriginalFilePath(), moveRenameClassObject.getOriginalFilePath());
        Assert.assertEquals(pushDownFieldObject.getOriginalClass(), moveRenameClassObject.getOriginalClassObject().getClassName());

        // Push down field A.foo -> B.foo
        pushDownFieldObject = new PushDownFieldObject("A", "foo", "B", "foo");

        cell.checkCombination(moveRenameClassObject2, pushDownFieldObject);

        // Should have changed A.foo -> B.foo to A.foo -> B0.foo
        Assert.assertEquals(pushDownFieldObject.getDestinationFilePath(), moveRenameClassObject2.getOriginalFilePath());
        Assert.assertEquals(pushDownFieldObject.getTargetSubClass(), moveRenameClassObject2.getOriginalClassObject().getClassName());


    }
}
