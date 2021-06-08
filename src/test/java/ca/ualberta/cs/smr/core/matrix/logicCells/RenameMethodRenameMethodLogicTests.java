package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class RenameMethodRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckRenameMethodRenameMethodOverrideConflict() {
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

    public void testCheckRenameMethodRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 3;
        Refactoring leftRefactoring = refactorings.get(0);
        Refactoring rightRefactoring = refactorings.get(2);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(new Node(leftRefactoring), new Node(rightRefactoring));
        Assert.assertFalse("Methods in the same class that do not have related names " +
                "before or after being refactored should not conflict", isConflicting);
    }

    public void testCheckRenameMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring leftRef = refactorings.get(1);
        Refactoring rightRef = refactorings.get(2);
        Node leftNode = new Node(leftRef);
        Node rightNode = new Node(rightRef);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(leftNode, rightNode);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        rightRef = refactorings.get(0);
        rightNode = new Node(rightRef);
        boolean expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(leftNode, rightNode);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(rightNode, leftNode);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(rightNode, rightNode);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", expectedFalse);
    }

    public void testNestedRenameMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> methodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefactorings != null;
        assert classRefactorings != null;
        Refactoring dispatcherRef = methodRefactorings.get(0);
        Refactoring rightRef = methodRefactorings.get(3);
        Refactoring classRef = classRefactorings.get(0);
        Node dispatcherNode = new Node(dispatcherRef);
        Node rightNode = new Node(rightRef);
        Node classNode = new Node(classRef);
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(classNode);
        rightNode.addDependsList(nodes);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkMethodNamingConflict(dispatcherNode, rightNode);
        Assert.assertTrue(isConflicting);
    }

    public void testFoundRenameMethodRenameMethodTransitivity() {
        // Rename method A.foo -> A.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        // Rename method A.bar -> A.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("A.java", "A",
                "bar", "A.java", "A", "foobar");
        // Rename Method A.foo -> A.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "A.java", "A", "foobar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testFoundRenameMethodRenameMethodTransitivity2() {
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");
        // Rename method B.bar -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("B.java", "B",
                "bar", "C.java", "C", "foobar");
        // Rename Method A.foo -> C.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "C.java", "C", "foobar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testNotFoundRenameMethodRenameMethodTransitivity() {
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");
        // Rename method B.buzz -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("B.java", "B",
                "buzz", "C.java", "C", "foobar");
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }

    private void doRenameMethodRenameMethodTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(null);
        boolean isTransitive = cell.checkRenameMethodRenameMethodTransitivity(firstRefactoring, secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
        }
        else {
            Assert.assertFalse(isTransitive);
        }
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationMethodName(),
                ((RenameMethodObject) firstRefactoring).getDestinationMethodName());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationClassName(),
                ((RenameMethodObject) firstRefactoring).getDestinationClassName());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getOriginalMethodName(),
                ((RenameMethodObject) firstRefactoring).getOriginalMethodName());
        Assert.assertNotEquals(((RenameMethodObject) expectedRefactoring).getOriginalMethodName(),
                ((RenameMethodObject) secondRefactoring).getOriginalMethodName());
    }
}
