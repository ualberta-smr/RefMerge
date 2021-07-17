package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ExtractMethodExtractMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckExtractMethodExtractMethodOverlappingFragmentsConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        RefactoringObject dispatcherObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(0));
        RefactoringObject receiverObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(1));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverlappingFragmentsConflict(dispatcherObject, receiverObject);
        Assert.assertFalse(isConflicting);
        dispatcherObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(7));
        isConflicting = cell.checkOverlappingFragmentsConflict(dispatcherObject, receiverObject);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckExtractMethodExtractMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/refactored";
        String configurePath = "extractMethodExtractMethodFiles/refactored/Override.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        RefactoringObject dispatcherObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(2));
        RefactoringObject receiverObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(3));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverrideConflict(dispatcherObject, receiverObject);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckExtractMethodExtractMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        RefactoringObject dispatcherObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(5));
        RefactoringObject receiverObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(6));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverloadConflict(dispatcherObject, receiverObject);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkOverloadConflict(dispatcherObject, dispatcherObject);
        Assert.assertFalse(isConflicting);
    }

    public void testCheckExtractMethodExtractMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodExtractMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;

        RefactoringObject dispatcherObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(5));
        RefactoringObject receiverObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(6));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkMethodNamingConflict(dispatcherObject, dispatcherObject);
        Assert.assertFalse(isConflicting);
        isConflicting = cell.checkMethodNamingConflict(dispatcherObject, receiverObject);
        Assert.assertFalse(isConflicting);

    }

}
