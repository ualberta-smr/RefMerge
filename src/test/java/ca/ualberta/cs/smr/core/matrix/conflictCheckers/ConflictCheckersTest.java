package ca.ualberta.cs.smr.core.matrix.conflictCheckers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ConflictCheckersTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testCheckNamingConflict() {
        Project project = myFixture.getProject();
        String originalElement = "foo";
        String originalVisitor = "bar";
        String refactoredElement = "newFoo";
        String refactoredVisitor = "newBar";
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        boolean expectedFalse = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings do not conflict", expectedFalse);

        originalVisitor = "foo";
        boolean expectedTrue = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because an element is renamed to two names", expectedTrue);
        refactoredVisitor = "newFoo";
        expectedFalse = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings are the same", expectedFalse);
        originalVisitor = "bar";
        expectedTrue = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
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
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Refactoring renameOtherFooMethod = refactorings.get(1);
        Refactoring renameChildBarMethod = refactorings.get(2);
        Refactoring renameOtherBarMethod = refactorings.get(3);
        Refactoring renameFooBarMethod = refactorings.get(4);
        boolean isConflicting = conflictCheckers.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameOtherFooMethod));
        Assert.assertFalse("Renamings in the same class should not result in override conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameOtherBarMethod));
        Assert.assertFalse("Methods that have no override relation, before or after, should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameFooBarMethod));
        Assert.assertFalse("Classes that have no inheritance should not result in override conflicts", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(new Node(renameParentFooMethod), new Node(renameChildBarMethod));
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testCheckOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        Refactoring changeOtherMethodToSecondOverloaded = refactorings.get(2);
        Refactoring changeOtherBarMethod = refactorings.get(3);
        boolean isConflicting = conflictCheckers.checkOverloadConflict(new Node(changeFirstOverloaded), new Node(changeOtherBarMethod));
        Assert.assertFalse("Methods in different classes should not cause overload conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverloadConflict(new Node(changeFirstOverloaded), new Node(changeOtherMethodToSecondOverloaded));
        Assert.assertFalse("Methods in the same class that do not have related names " +
                "before or after being refactored should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverloadConflict(new Node(changeSecondOverloaded), new Node(changeOtherMethodToSecondOverloaded));
        Assert.assertTrue("Methods that overload after refactoring should conflict", isConflicting);
    }

    public void testCheckMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        Refactoring visitorRef = refactorings.get(2);
        Node elementNode = new Node(elementRef);
        Node visitorNode = new Node(visitorRef);
        boolean expectedFalse = conflictCheckers.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        visitorRef = refactorings.get(0);
        visitorNode = new Node(visitorRef);
        boolean expectedTrue = conflictCheckers.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = conflictCheckers.checkMethodNamingConflict(visitorNode, elementNode);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = conflictCheckers.checkMethodNamingConflict(visitorNode, visitorNode);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", expectedFalse);
    }

    public void testNestedMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
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
        visitorNode.updateHead(classNode);
        visitorNode.addToDependsList(classNode);
        boolean isConflicting = conflictCheckers.checkMethodNamingConflict(elementNode, visitorNode);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckClassNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(project);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        boolean isConflicting = conflictCheckers.checkClassNamingConflict(new Node(foo), new Node(bar));
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkClassNamingConflict(new Node(foo), new Node(foo2));
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }
}
