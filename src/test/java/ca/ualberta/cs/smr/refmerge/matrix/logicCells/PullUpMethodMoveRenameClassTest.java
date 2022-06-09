package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class PullUpMethodMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Pull up method A.foo -> B.foo
        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("A", "foo", "B", "foo");
        // Rename and move A0 -> A
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A0",
                "p", "A", "A", "p");

        // Rename B0 -> B
        MoveRenameClassObject moveRenameClassObject2 = new MoveRenameClassObject("B.java", "B0",
                "p", "B", "B", "p");
        PullUpMethodMoveRenameClassCell cell = new PullUpMethodMoveRenameClassCell(null);
        cell.checkCombination(moveRenameClassObject, pullUpMethodObject);

        Assert.assertEquals(pullUpMethodObject.getOriginalFilePath(), moveRenameClassObject.getOriginalFilePath());
        Assert.assertEquals(pullUpMethodObject.getOriginalClass(), moveRenameClassObject.getOriginalClassObject().getClassName());

        // Pull up method A.foo -> B.foo
        pullUpMethodObject = new PullUpMethodObject("A", "foo", "B", "foo");

        cell.checkCombination(moveRenameClassObject2, pullUpMethodObject);

        Assert.assertEquals(pullUpMethodObject.getDestinationFilePath(), moveRenameClassObject2.getOriginalFilePath());
        Assert.assertEquals(pullUpMethodObject.getTargetClass(), moveRenameClassObject2.getOriginalClassObject().getClassName());


    }

}
