package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ExtractMethodRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckExtractMethodRenameMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";

        String configurePath = "extractMethodRenameMethodFiles/refactored/OverloadInheritance.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(0));
        Node renameMethodNode = new Node(renameMethodRefactorings.get(0));
        ExtractMethodRenameMethodCell cell = new ExtractMethodRenameMethodCell(project);
        boolean isDependent = cell.checkOverrideConflict(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
        renameMethodNode = new Node(renameMethodRefactorings.get(2));
        isDependent = cell.checkOverrideConflict(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
        extractMethodNode = new Node(extractMethodRefactorings.get(3));
        renameMethodNode = new Node(renameMethodRefactorings.get(5));
        isDependent = cell.checkOverrideConflict(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
        extractMethodNode = new Node(extractMethodRefactorings.get(1));
        renameMethodNode = new Node(renameMethodRefactorings.get(3));

        configurePath = "extractMethodRenameMethodFiles/refactored/Override.java";
        myFixture.configureByFiles(configurePath);

        isDependent = cell.checkOverrideConflict(renameMethodNode, extractMethodNode);
        Assert.assertTrue(isDependent);
    }

    public void testCheckExtractMethodRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";

        String configurePath = "extractMethodRenameMethodFiles/refactored/OverloadInheritance.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(0));
        Node renameMethodNode = new Node(renameMethodRefactorings.get(0));
        ExtractMethodRenameMethodCell cell = new ExtractMethodRenameMethodCell(project);
        boolean isDependent = cell.checkOverloadConflict(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
        renameMethodNode = new Node(renameMethodRefactorings.get(2));
        isDependent = cell.checkOverloadConflict(renameMethodNode, extractMethodNode);
        Assert.assertTrue(isDependent);
        extractMethodNode = new Node(extractMethodRefactorings.get(3));
        renameMethodNode = new Node(renameMethodRefactorings.get(5));
        isDependent = cell.checkOverloadConflict(renameMethodNode, extractMethodNode);
        Assert.assertTrue(isDependent);
    }

    public void testCheckExtractMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(2));
        Node renameMethodNode = new Node(renameMethodRefactorings.get(4));
        ExtractMethodRenameMethodCell cell = new ExtractMethodRenameMethodCell(project);
        boolean isDependent = cell.checkMethodNamingConflict(renameMethodNode, extractMethodNode);
        Assert.assertTrue(isDependent);
        renameMethodNode = new Node(renameMethodRefactorings.get(3));
        isDependent = cell.checkMethodNamingConflict(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
    }

    public void testCheckExtractMethodRenameMethodDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(0));
        Node renameMethodNode = new Node(renameMethodRefactorings.get(1));
        boolean isDependent = ExtractMethodRenameMethodCell.checkExtractMethodRenameMethodDependence(renameMethodNode,
                extractMethodNode);
        Assert.assertTrue(isDependent);
        renameMethodNode = new Node(renameMethodRefactorings.get(2));
        isDependent = ExtractMethodRenameMethodCell.checkExtractMethodRenameMethodDependence(renameMethodNode, extractMethodNode);
        Assert.assertFalse(isDependent);
    }
}
