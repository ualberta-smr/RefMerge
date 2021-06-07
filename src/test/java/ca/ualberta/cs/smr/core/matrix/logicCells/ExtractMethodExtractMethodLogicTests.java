package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
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
        Node node1 = new Node(extractMethodRefactorings.get(0));
        Node node2 = new Node(extractMethodRefactorings.get(1));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverlappingFragmentsConflict(node1, node2);
        Assert.assertFalse(isConflicting);
        node1 = new Node(extractMethodRefactorings.get(7));
        isConflicting = cell.checkOverlappingFragmentsConflict(node1, node2);
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
        Node node1 = new Node(extractMethodRefactorings.get(2));
        Node node2 = new Node(extractMethodRefactorings.get(3));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverrideConflict(node1, node2);
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
        Node node1 = new Node(extractMethodRefactorings.get(5));
        Node node2 = new Node(extractMethodRefactorings.get(6));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkOverloadConflict(node1, node2);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.checkOverloadConflict(node1, node1);
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
        Node node1 = new Node(extractMethodRefactorings.get(5));
        ExtractMethodExtractMethodCell cell = new ExtractMethodExtractMethodCell(project);
        boolean isConflicting = cell.checkMethodNamingConflict(node1, node1);
        Assert.assertTrue(isConflicting);
        Node node2 = new Node(extractMethodRefactorings.get(6));
        isConflicting = cell.checkMethodNamingConflict(node1, node2);
        Assert.assertFalse(isConflicting);

    }

}
