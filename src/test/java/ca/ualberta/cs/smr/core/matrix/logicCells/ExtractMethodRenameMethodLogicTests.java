package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
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

    public void testCheckExtractMethodRenameMethodTransitivity() {
        // Extract method A.efoo from A.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject();
        extractMethodObject.setOriginalFilePath("A.java");
        extractMethodObject.setDestinationFilePath("A.java");
        extractMethodObject.setOriginalClassName("A");
        extractMethodObject.setDestinationClassName("A");
        extractMethodObject.setOriginalMethodName("foo");
        extractMethodObject.setDestinationMethodName("efoo");
        // Rename method A.efoo -> A.ebar
        RenameMethodObject renameMethodObject = new RenameMethodObject();
        renameMethodObject.setOriginalFilePath("A.java");
        renameMethodObject.setDestinationFilePath("A.java");
        renameMethodObject.setOriginalClassName("A");
        renameMethodObject.setDestinationClassName("A");
        renameMethodObject.setOriginalMethodName("efoo");
        renameMethodObject.setDestinationMethodName("ebar");
        // Extract method A.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setDestinationFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setDestinationClassName("A");
        expectedRefactoring.setOriginalMethodName("foo");
        expectedRefactoring.setDestinationMethodName("ebar");

        doExtractMethodRenameMethodTest(renameMethodObject, extractMethodObject, expectedRefactoring, true);
    }

    public void testCHeckExtractMethodRenameMethodCombination() {
        // Extract method A.ebar from A.bar
        ExtractMethodObject extractMethodObject = new ExtractMethodObject();
        extractMethodObject.setOriginalFilePath("A.java");
        extractMethodObject.setDestinationFilePath("A.java");
        extractMethodObject.setOriginalClassName("A");
        extractMethodObject.setDestinationClassName("A");
        extractMethodObject.setOriginalMethodName("bar");
        extractMethodObject.setDestinationMethodName("ebar");
        // Rename method A.foo -> A.bar
        RenameMethodObject renameMethodObject = new RenameMethodObject();
        renameMethodObject.setOriginalFilePath("A.java");
        renameMethodObject.setDestinationFilePath("A.java");
        renameMethodObject.setOriginalClassName("A");
        renameMethodObject.setDestinationClassName("A");
        renameMethodObject.setOriginalMethodName("foo");
        renameMethodObject.setDestinationMethodName("bar");
        // Extract method A.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setDestinationFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setDestinationClassName("A");
        expectedRefactoring.setOriginalMethodName("foo");
        expectedRefactoring.setDestinationMethodName("ebar");

        doExtractMethodRenameMethodTest(renameMethodObject, extractMethodObject, expectedRefactoring, false);
    }


    private void doExtractMethodRenameMethodTest(RefactoringObject renameMethodObject, RefactoringObject extractMethodObject,
                                               RefactoringObject expectedRefactoring, boolean expectedTransitivity) {

        boolean isTransitive = ExtractMethodRenameMethodCell.checkExtractMethodRenameMethodTransitivity(renameMethodObject,
                extractMethodObject);

        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
        }
        else {
            Assert.assertFalse(isTransitive);
        }
        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), extractMethodObject.getDestinationFilePath());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getOriginalClassName(),
                ((ExtractMethodObject) extractMethodObject).getOriginalClassName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getDestinationClassName(),
                ((ExtractMethodObject) extractMethodObject).getDestinationClassName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getOriginalMethodName(),
                ((ExtractMethodObject) extractMethodObject).getOriginalMethodName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getDestinationMethodName(),
                ((ExtractMethodObject) extractMethodObject).getDestinationMethodName());
    }

}
