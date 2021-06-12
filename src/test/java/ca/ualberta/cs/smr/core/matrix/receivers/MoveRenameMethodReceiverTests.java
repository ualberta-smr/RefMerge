package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

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

    public void testRenameMethodRenameMethodOverrideConflict() {
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

    public void testRenameMethodRenameMethodOverloadConflict() {
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

    public void testRenameMethodRenameMethodNamingConflict() {
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

    public void testRenameMethodRenameMethodNoConflict() {
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

}
