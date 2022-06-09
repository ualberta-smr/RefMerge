package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownMethodMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Push down method A.foo -> B.foo
        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");
        // Rename and move A0 -> A
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A0",
                "p", "A", "A", "p");

        // Rename B0 -> B
        MoveRenameClassObject moveRenameClassObject2 = new MoveRenameClassObject("B.java", "B0",
                "p", "B", "B", "p");
        PushDownMethodMoveRenameClassCell cell = new PushDownMethodMoveRenameClassCell(null);
        cell.checkCombination(moveRenameClassObject, pushDownMethodObject);

        Assert.assertEquals(pushDownMethodObject.getOriginalFilePath(), moveRenameClassObject.getOriginalFilePath());
        Assert.assertEquals(pushDownMethodObject.getOriginalClass(), moveRenameClassObject.getOriginalClassObject().getClassName());

        // Pull up method A.foo -> B.foo
        pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");

        cell.checkCombination(moveRenameClassObject2, pushDownMethodObject);

        Assert.assertEquals(pushDownMethodObject.getDestinationFilePath(), moveRenameClassObject2.getOriginalFilePath());
        Assert.assertEquals(pushDownMethodObject.getTargetBaseClass(), moveRenameClassObject2.getOriginalClassObject().getClassName());


    }
}
