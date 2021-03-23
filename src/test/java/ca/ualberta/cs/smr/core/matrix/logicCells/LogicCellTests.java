package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.MatrixUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class LogicCellTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testCheckNamingConflict() {
        String originalElement = "foo";
        String originalVisitor = "bar";
        String refactoredElement = "newFoo";
        String refactoredVisitor = "newBar";
        boolean expectedFalse = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings do not conflict", expectedFalse);

        originalVisitor = "foo";
        boolean expectedTrue = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because an element is renamed to two names", expectedTrue);
        refactoredVisitor = "newFoo";
        expectedFalse = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings are the same", expectedFalse);
        originalVisitor = "bar";
        expectedTrue = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because two elements are renamed to the same name", expectedTrue);
    }

    public void testCheckOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Refactoring renameOtherFooMethod = refactorings.get(1);
        Refactoring renameChildBarMethod = refactorings.get(2);
        Refactoring renameOtherBarMethod = refactorings.get(3);
        Refactoring renameFooBarMethod = refactorings.get(4);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameOtherFooMethod));
        Assert.assertFalse("Renamings in the same class should not result in override conflict", isConflicting);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameOtherBarMethod));
        Assert.assertFalse("Methods that have no override relation, before or after, should not conflict", isConflicting);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameFooBarMethod));
        Assert.assertFalse("Classes that have no inheritance should not result in override conflicts", isConflicting);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameChildBarMethod));
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testCheckOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        Refactoring changeOtherMethodToSecondOverloaded = refactorings.get(2);
        Refactoring changeOtherBarMethod = refactorings.get(3);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(new Node(changeFirstOverloaded), new Node(changeOtherBarMethod));
        Assert.assertFalse("Methods in different classes should not cause overload conflict", isConflicting);
        isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(new Node(changeFirstOverloaded), new Node(changeOtherMethodToSecondOverloaded));
        Assert.assertFalse("Methods in the same class that do not have related names " +
                "before or after being refactored should not conflict", isConflicting);
        isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(new Node(changeSecondOverloaded), new Node(changeOtherMethodToSecondOverloaded));
        Assert.assertTrue("Methods that overload after refactoring should conflict", isConflicting);
    }

    public void testCheckMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        Refactoring visitorRef = refactorings.get(2);
        Node elementNode = new Node(elementRef);
        Node visitorNode = new Node(visitorRef);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        visitorRef = refactorings.get(0);
        visitorNode = new Node(visitorRef);
        boolean expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(visitorNode, elementNode);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(visitorNode, visitorNode);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", expectedFalse);
    }

    public void testNestedMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> methodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefactorings != null;
        assert classRefactorings != null;
        Refactoring elementRef = methodRefactorings.get(0);
        Refactoring visitorRef = methodRefactorings.get(3);
        Refactoring classRef = classRefactorings.get(0);
        Node elementNode = new Node(elementRef);
        Node visitorNode = new Node(visitorRef);
        Node classNode = new Node(classRef);
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(classNode);
        visitorNode.addDependsList(nodes);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckClassNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        boolean isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(new Node(foo), new Node(bar));
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(new Node(foo), new Node(foo2));
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }


    public void testCheckRenameMethodRenameClassDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring methodRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring classRef = classRefs.get(0);
        Node classNode = new Node(classRef);
        Node methodNode = new Node(methodRef);
        boolean isDependent = RenameClassRenameMethodCell.checkRenameMethodRenameClassDependence(classNode, methodNode);
        Assert.assertTrue(isDependent);
    }

}
