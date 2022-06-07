package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class PullUpMethodMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: Rename A.foo to A.bar
        MoveRenameMethodObject moveRenameMethodObject1 = new MoveRenameMethodObject("",
                "A", foo, "", "A", bar);
        // Ref 3: Move C.foo to B.foo
        MoveRenameMethodObject moveRenameMethodObject2 = new MoveRenameMethodObject("",
                "C", foo, "", "B", foo);
        // Ref 3: Move C.foo to D.foo
        MoveRenameMethodObject moveRenameMethodObject3 = new MoveRenameMethodObject("",
                "C", foo, "", "D", foo);
        PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(moveRenameMethodObject1, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject2, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(moveRenameMethodObject3, pullUpMethodObject1);
        Assert.assertFalse(isConflicting);
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
        MoveRenameMethodObject pullUpObject = null;
        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("addNumbers") && newName.equals("numbers")) {
                renameObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("doNumbers") && newName.equals("numbers")) {
                pullUpObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("ChildClass", "doNumbers", "ChildClass", "numbers");

        assert pullUpObject != null;
        pullUpMethodObject.setDestinationFilePath(pullUpObject.getDestinationFilePath());
        pullUpMethodObject.setOriginalMethodSignature(pullUpObject.getOriginalMethodSignature());
        pullUpMethodObject.setDestinationMethodSignature(pullUpObject.getDestinationMethodSignature());
        pullUpMethodObject.setOriginalFilePath(pullUpObject.getOriginalFilePath());

        PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(project);
        assert renameObject != null;
        boolean isConflict = cell.overrideConflict(renameObject, pullUpMethodObject);
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
        MoveRenameMethodObject pullUpObject = null;

        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("sumNumbers") && newName.equals("numbers")) {
                moveRenameObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("multNumbers") && newName.equals("numbers")) {
                pullUpObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("Foo",
                "multNumbers", "Foo", "numbers");

        assert pullUpObject != null;
        pullUpMethodObject.setDestinationFilePath(pullUpObject.getDestinationFilePath());
        pullUpMethodObject.setOriginalMethodSignature(pullUpObject.getOriginalMethodSignature());
        pullUpMethodObject.setDestinationMethodSignature(pullUpObject.getDestinationMethodSignature());
        pullUpMethodObject.setOriginalFilePath(pullUpObject.getOriginalFilePath());

        PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(project);

        assert moveRenameObject != null;
        boolean isConflicting = cell.overloadConflict(moveRenameObject, pullUpMethodObject);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Pull up method A.foo -> B.foo
        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("A", "foo", "B", "foo");
        // Rename and move B.foo -> C.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("B.java",
                "B", foo, "C.java", "C", bar);
        PullUpMethodMoveRenameMethodCell cell = new PullUpMethodMoveRenameMethodCell(null);
        cell.checkCombination(moveRenameMethodObject, pullUpMethodObject);

        Assert.assertEquals(pullUpMethodObject.getDestinationFilePath(), moveRenameMethodObject.getDestinationFilePath());
        Assert.assertEquals(pullUpMethodObject.getOriginalFilePath(), moveRenameMethodObject.getOriginalFilePath());
        Assert.assertEquals(pullUpMethodObject.getOriginalMethodSignature(), moveRenameMethodObject.getOriginalMethodSignature());
        Assert.assertEquals(pullUpMethodObject.getDestinationMethodSignature(), moveRenameMethodObject.getDestinationMethodSignature());
        Assert.assertEquals(pullUpMethodObject.getOriginalClass(), moveRenameMethodObject.getOriginalClassName());

        // Pull up method A.foo -> B.foo
        pullUpMethodObject = new PullUpMethodObject("A", "foo", "B", "foo");
        // Rename and move Z.bar -> A.foo
        moveRenameMethodObject = new MoveRenameMethodObject("Z.java",
                "Z", bar, "A.java", "A", foo);
        cell.checkCombination(moveRenameMethodObject, pullUpMethodObject);

        Assert.assertEquals(pullUpMethodObject.getDestinationFilePath(), moveRenameMethodObject.getDestinationFilePath());
        Assert.assertEquals(pullUpMethodObject.getOriginalFilePath(), moveRenameMethodObject.getOriginalFilePath());
        Assert.assertEquals(pullUpMethodObject.getOriginalMethodSignature(), moveRenameMethodObject.getOriginalMethodSignature());
        Assert.assertEquals(pullUpMethodObject.getDestinationMethodSignature(), moveRenameMethodObject.getDestinationMethodSignature());
        Assert.assertEquals(pullUpMethodObject.getOriginalClass(), moveRenameMethodObject.getOriginalClassName());

    }

}
