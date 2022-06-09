package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;


public class PullUpFieldPullUpFieldTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        // Ref 1: A.foo pulled up to B.foo
        PullUpFieldObject pullUpFieldObject1 = new PullUpFieldObject("A", "foo", "B", "foo");
        // Ref 2: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject2 = new PullUpFieldObject("A", "foo", "C", "foo");
        // Ref 3: A.foo pulled up to C.foo (conflicts with ref 1)
        PullUpFieldObject pullUpFieldObject3 = new PullUpFieldObject("A", "foo", "C", "foo");

        PullUpFieldPullUpFieldCell cell = new PullUpFieldPullUpFieldCell(getProject());

        boolean isConflicting = cell.conflictCell(pullUpFieldObject1, pullUpFieldObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pullUpFieldObject2, pullUpFieldObject3);
        Assert.assertFalse(isConflicting);
    }


    public void testShadowConflict() {
        Project project = myFixture.getProject();
        // Reuse field shadowing files to get PSI structure for shadow test
        String configurePath = "fieldShadowingFiles/Main.java";
        myFixture.configureByFiles(configurePath);

        PullUpFieldObject pullUpFieldObject = new PullUpFieldObject("Main", "aField", "Main", "aField");
        pullUpFieldObject.setOriginalFilePath(configurePath);
        pullUpFieldObject.setDestinationFilePath(configurePath);
        PullUpFieldObject pullUpFieldObject2 = new PullUpFieldObject("SubClass", "aField", "SubClass", "aField");
        pullUpFieldObject2.setOriginalFilePath(configurePath);
        pullUpFieldObject2.setDestinationFilePath(configurePath);

        PullUpFieldPullUpFieldCell cell = new PullUpFieldPullUpFieldCell(project);
        boolean isConflict = cell.shadowConflict(pullUpFieldObject, pullUpFieldObject2);
        Assert.assertTrue(isConflict);
    }

}
