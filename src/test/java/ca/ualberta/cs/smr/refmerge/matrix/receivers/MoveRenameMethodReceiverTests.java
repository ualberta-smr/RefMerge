package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.logicCells.MoveRenameMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;


public class MoveRenameMethodReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(ref);
        MoveRenameMethodReceiver receiver = new MoveRenameMethodReceiver();
        receiver.set(leftRefactoring, null);
        Assert.assertNotNull("The refactoring element should not be null", receiver.refactoringObject);
    }

    public void testMoveRenameMethodMoveRenameMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(renameParentFooMethod);
        Refactoring renameChildBarMethod = refactorings.get(2);
        RefactoringObject rightRefactoring = RefactoringObjectUtils.createRefactoringObject(renameChildBarMethod);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(leftRefactoring, project, false);

        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = cell.moveRenameMethodMoveRenameMethodConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testMoveRenameMethodMoveRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 3;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(changeFirstOverloaded);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        RefactoringObject rightRefactoring = RefactoringObjectUtils.createRefactoringObject(changeSecondOverloaded);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(leftRefactoring, project, false);
        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = cell.moveRenameMethodMoveRenameMethodConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue("Methods that start overloaded and get changed to different names should conflict", isConflicting);
    }

    public void testMoveRenameMethodMoveRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring leftRef = refactorings.get(0);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(leftRef);
        Refactoring rightRef = refactorings.get(1);
        RefactoringObject rightRefactoring = RefactoringObjectUtils.createRefactoringObject(rightRef);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(leftRefactoring, project, false);
        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = cell.moveRenameMethodMoveRenameMethodConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", isConflicting);
    }

    public void testMoveRenameMethodMoveRenameMethodNoConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring leftRef = refactorings.get(1);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(leftRef);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(leftRefactoring, project, false);
        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = cell.moveRenameMethodMoveRenameMethodConflictCell(leftRefactoring, leftRefactoring);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", isConflicting);
    }

    public void testMoveRenameMethodMoveRenameMethodDependence() {
        Project project = myFixture.getProject();
        List<ParameterObject> parameters = new ArrayList<>();
        parameters.add(new ParameterObject("int", "return"));
        parameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(parameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(parameters, "bar");
        // (1) A.foo -> A.bar
        MoveRenameMethodObject leftMethodObject1 = new MoveRenameMethodObject("A.java", "A", foo,
                "A.java", "A", bar);
        leftMethodObject1.setType(RefactoringType.RENAME_METHOD);
        // (1) A.foo -> B.foo
        MoveRenameMethodObject rightMethodObject1 = new MoveRenameMethodObject("A.java", "A", foo,
                "B.java", "B", foo);
        rightMethodObject1.setType(RefactoringType.MOVE_OPERATION);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(leftMethodObject1, project,false);
        MoveRenameMethodReceiver receiver = new MoveRenameMethodReceiver();
        receiver.set(rightMethodObject1);
        dispatcher.dispatch(receiver);

        MethodSignatureObject leftSignature = leftMethodObject1.getDestinationMethodSignature();
        MethodSignatureObject rightSignature = rightMethodObject1.getDestinationMethodSignature();
        String leftClass = leftMethodObject1.getDestinationClassName();
        String rightClass = rightMethodObject1.getDestinationClassName();
        Assert.assertEquals(leftSignature, rightSignature);
        Assert.assertEquals(leftClass, rightClass);

    }

}
