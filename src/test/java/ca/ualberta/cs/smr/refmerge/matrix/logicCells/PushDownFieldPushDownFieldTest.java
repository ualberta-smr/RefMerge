package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class PushDownFieldPushDownFieldTest extends LightJavaCodeInsightFixtureTestCase {

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
        PushDownFieldObject pushDownFieldObject1 = new PushDownFieldObject("SubClass", "aField", "SubClass", "aField");
        pushDownFieldObject1.setOriginalFilePath(configurePath);
        pushDownFieldObject1.setDestinationFilePath(configurePath);

        PushDownFieldPushDownFieldCell cell = new PushDownFieldPushDownFieldCell(project);
        boolean isConflict = cell.shadowConflict(pushDownFieldObject, pushDownFieldObject1);
        Assert.assertTrue(isConflict);

        pushDownFieldObject = new PushDownFieldObject("Main", "doubleField", "Main", "doubleField");
        pushDownFieldObject.setOriginalFilePath(configurePath);
        pushDownFieldObject.setDestinationFilePath(configurePath);
        isConflict = cell.shadowConflict(pushDownFieldObject, pushDownFieldObject1);
        Assert.assertFalse(isConflict);
    }

    public void testNamingConflict() {
        // Ref 1: Field A.foo pushed down to B.foo
        PushDownFieldObject pushDownMethodObject1 = new PushDownFieldObject("A", "foo", "B", "foo");
        // Ref 2: Field A.foo pushed down to C.foo
        PushDownFieldObject pushDownMethodObject2 = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 3: Field D.foo pushed down to C.foo (conflicts with ref 2)
        PushDownFieldObject pushDownMethodObject3 = new PushDownFieldObject("D", "foo", "C", "foo");

        PushDownFieldPushDownFieldCell cell = new PushDownFieldPushDownFieldCell(getProject());

        boolean isConflicting = cell.namingConflict(pushDownMethodObject2, pushDownMethodObject3);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(pushDownMethodObject1, pushDownMethodObject2);
        Assert.assertFalse(isConflicting);
    }

    public void testCheckTransitivity() {
        // Ref 1: Field A.foo pushed down to B.foo
        PushDownFieldObject pushDownFieldObject1 = new PushDownFieldObject("A", "foo", "C", "foo");
        // Ref 2: Field C.foo pushed down to B.foo
        PushDownFieldObject pushDownFieldObject2 = new PushDownFieldObject("A", "foo", "B", "foo");
        pushDownFieldObject2.addSubClass("D", "D");
        pushDownFieldObject1.addSubClass("D", "D");

        PushDownFieldPushDownFieldCell cell = new PushDownFieldPushDownFieldCell(getProject());

        boolean isTransitive= cell.checkTransitivity(pushDownFieldObject1, pushDownFieldObject2);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(pushDownFieldObject1.getSubClasses().size(), 3);
        Assert.assertEquals(pushDownFieldObject2.getSubClasses().size(), 3);
    }
}
