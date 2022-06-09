package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldPullUpFieldTest extends LightJavaCodeInsightFixtureTestCase {

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
        PushDownFieldObject pushDownFieldObject1 = new PushDownFieldObject("SubClass", "aField", "SubClass", "aField");
        pushDownFieldObject1.setOriginalFilePath(configurePath);
        pushDownFieldObject1.setDestinationFilePath(configurePath);

        PushDownFieldPullUpFieldCell cell = new PushDownFieldPullUpFieldCell(project);
        boolean isConflict = cell.shadowConflict(pullUpFieldObject, pushDownFieldObject1);
        Assert.assertTrue(isConflict);

        pullUpFieldObject = new PullUpFieldObject("Main", "doubleField", "Main", "doubleField");
        pullUpFieldObject.setOriginalFilePath(configurePath);
        pullUpFieldObject.setDestinationFilePath(configurePath);
        isConflict = cell.shadowConflict(pullUpFieldObject, pushDownFieldObject1);
        Assert.assertFalse(isConflict);
    }

    public void testNamingConflict() {
        // Ref 1: A.foo pulled up to B.foo
        PullUpFieldObject pullUpFieldObject1 = new PullUpFieldObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pushed down to C.foo (Conflicts with Ref 1)
        PushDownFieldObject pushDownFieldObject = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 3: C.foo pushed down to B.foo (Conflicts with Ref 1)
        PushDownFieldObject pushDownFieldObject1 = new PushDownFieldObject("C", "foo", "B", "foo");
        // Ref 4: A.bar pushed down to C.bar
        PushDownFieldObject pushDownFieldObject2 = new PushDownFieldObject("A", "bar", "C", "bar");
        // Ref 4: B.bar pushed down to C.bar
        PushDownFieldObject pushDownFieldObject3 = new PushDownFieldObject("B", "bar", "C", "bar");


        PushDownFieldPullUpFieldCell cell = new PushDownFieldPullUpFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(pullUpFieldObject1, pushDownFieldObject);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpFieldObject1, pushDownFieldObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpFieldObject1, pushDownFieldObject2);
        Assert.assertFalse(isConflicting);
        isConflicting = cell.conflictCell(pullUpFieldObject1, pushDownFieldObject3);
        Assert.assertFalse(isConflicting);
    }
}
