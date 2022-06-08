package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class PushDownMethodMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        // Reuse rename method files to get PSI structure for override test
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        MoveRenameMethodObject renameObject = null;
        MoveRenameMethodObject pushDownObject = null;
        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("addNumbers") && newName.equals("numbers")) {
                renameObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("doNumbers") && newName.equals("numbers")) {
                pushDownObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("ChildClass", "doNumbers", "ChildClass", "numbers");

        assert pushDownObject != null;
        pushDownMethodObject.setDestinationFilePath(pushDownObject.getDestinationFilePath());
        pushDownMethodObject.setOriginalMethodSignature(pushDownObject.getOriginalMethodSignature());
        pushDownMethodObject.setDestinationMethodSignature(pushDownObject.getDestinationMethodSignature());
        pushDownMethodObject.setOriginalFilePath(pushDownObject.getOriginalFilePath());

        PushDownMethodMoveRenameMethodCell cell = new PushDownMethodMoveRenameMethodCell(project);
        assert renameObject != null;
        boolean isConflict = cell.overrideConflict(renameObject, pushDownMethodObject);
        Assert.assertTrue(isConflict);
    }

    public void testOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 3;

        MoveRenameMethodObject moveRenameObject = null;
        MoveRenameMethodObject pushDownObject = null;

        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("sumNumbers") && newName.equals("numbers")) {
                moveRenameObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("multNumbers") && newName.equals("numbers")) {
                pushDownObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("Foo",
                "multNumbers", "Foo", "numbers");

        assert pushDownObject != null;
        pushDownMethodObject.setDestinationFilePath(pushDownObject.getDestinationFilePath());
        pushDownMethodObject.setOriginalMethodSignature(pushDownObject.getOriginalMethodSignature());
        pushDownMethodObject.setDestinationMethodSignature(pushDownObject.getDestinationMethodSignature());
        pushDownMethodObject.setOriginalFilePath(pushDownObject.getOriginalFilePath());

        PushDownMethodMoveRenameMethodCell cell = new PushDownMethodMoveRenameMethodCell(project);

        assert moveRenameObject != null;
        boolean isConflicting = cell.overloadConflict(moveRenameObject, pushDownMethodObject);
        Assert.assertTrue(isConflicting);
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pushed down to B.foo
        PushDownMethodObject pushDownMethodObject1 = new PushDownMethodObject("A", "foo", "B", "foo");
        // Ref 2: Rename A.foo to A.bar
        MoveRenameMethodObject moveRenameMethodObject1 = new MoveRenameMethodObject("",
                "A", foo, "", "A", bar);
        // Ref 3: Move C.foo to B.foo
        MoveRenameMethodObject moveRenameMethodObject2 = new MoveRenameMethodObject("",
                "C", foo, "", "B", foo);
        // Ref 3: Move C.foo to D.foo
        MoveRenameMethodObject moveRenameMethodObject3 = new MoveRenameMethodObject("",
                "C", foo, "", "D", foo);

        PushDownMethodMoveRenameMethodCell cell = new PushDownMethodMoveRenameMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameMethodObject1, pushDownMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject2, pushDownMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject3, pushDownMethodObject1);
        Assert.assertFalse(isConflicting);
    }
}
