package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PullUpFieldMoveRenameFieldTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testShadowConflict() {
        Project project = myFixture.getProject();
        // Reuse field shadowing files to get PSI structure for shadow test
        String configurePath = "fieldShadowingFiles/Main.java";
        myFixture.configureByFiles(configurePath);

        PullUpFieldObject pullUpFieldObject = new PullUpFieldObject("Main", "aField", "Main", "aField");
        pullUpFieldObject.setOriginalFilePath(configurePath);
        pullUpFieldObject.setDestinationFilePath(configurePath);
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("", "SubClass", "aField", "", "SubClass", "aField");
        moveRenameFieldObject.setOriginalFilePath(configurePath);
        moveRenameFieldObject.setDestinationFilePath(configurePath);

        PullUpFieldMoveRenameFieldCell cell = new PullUpFieldMoveRenameFieldCell(project);
        boolean isConflict = cell.shadowConflict(moveRenameFieldObject, pullUpFieldObject);
        Assert.assertTrue(isConflict);

        pullUpFieldObject = new PullUpFieldObject("Main", "doubleField", "Main", "doubleField");
        pullUpFieldObject.setOriginalFilePath(configurePath);
        pullUpFieldObject.setDestinationFilePath(configurePath);
        isConflict = cell.shadowConflict(moveRenameFieldObject, pullUpFieldObject);
        Assert.assertFalse(isConflict);

    }

    public void testNamingConflict() {
        // Ref 1: A.foo moved and renamed to B.bar
        MoveRenameFieldObject moveRenameFieldObject =
                new MoveRenameFieldObject("A.java", "A", "foo", "B.java","B", "bar");
        // Ref 2: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject2 = new PullUpFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.bar pulled up to B.bar (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject3 = new PullUpFieldObject("A", "bar", "B", "bar");
        // Ref 3: B.bar pulled up to A.foo
        PullUpFieldObject pullUpFieldObject4 = new PullUpFieldObject("B", "bar", "A", "foo");


        PullUpFieldMoveRenameFieldCell cell = new PullUpFieldMoveRenameFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameFieldObject, pullUpFieldObject4);
        Assert.assertFalse(isConflicting);
    }
}
