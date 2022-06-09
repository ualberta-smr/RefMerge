package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldMoveRenameFieldTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testShadowConflict() {
        Project project = myFixture.getProject();
        // Reuse field shadowing files to get PSI structure for shadow test
        String configurePath = "fieldShadowingFiles/Main.java";
        myFixture.configureByFiles(configurePath);

        PushDownFieldObject pushDownFieldObject = new PushDownFieldObject("Main", "aField", "Main", "aField");
        pushDownFieldObject.setOriginalFilePath(configurePath);
        pushDownFieldObject.setDestinationFilePath(configurePath);
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("", "SubClass", "aField", "", "SubClass", "aField");
        moveRenameFieldObject.setOriginalFilePath(configurePath);
        moveRenameFieldObject.setDestinationFilePath(configurePath);

        PushDownFieldMoveRenameFieldCell cell = new PushDownFieldMoveRenameFieldCell(project);
        boolean isConflict = cell.shadowConflict(moveRenameFieldObject, pushDownFieldObject);
        Assert.assertTrue(isConflict);

        pushDownFieldObject = new PushDownFieldObject("Main", "doubleField", "Main", "doubleField");
        pushDownFieldObject.setOriginalFilePath(configurePath);
        pushDownFieldObject.setDestinationFilePath(configurePath);
        isConflict = cell.shadowConflict(moveRenameFieldObject, pushDownFieldObject);
        Assert.assertFalse(isConflict);

    }

    public void testNamingConflict() {
        // Ref 1: A.foo moved and renamed to B.bar
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("A.java", "A", "foo", "B.java","B", "bar");
        // Ref 2: A.foo pushed down to C.foo (conflicts with ref 1)
        PushDownFieldObject pushDownFieldObject2 = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.bar pushed down to B.bar (conflicts with ref 1)
        PushDownFieldObject pushDownFieldObject3 = new PushDownFieldObject("A", "bar", "B", "bar");
        // Ref 3: B.bar pushed down to A.foo
        PushDownFieldObject pushDownFieldObject4 = new PushDownFieldObject("B", "bar", "A", "foo");


        PushDownFieldMoveRenameFieldCell cell = new PushDownFieldMoveRenameFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pushDownFieldObject4);
        Assert.assertFalse(isConflicting);
    }
}
