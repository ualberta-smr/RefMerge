package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameMethodRenameMethodCell;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class RenameMethodReceiverTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodReceiver receiver = new RenameMethodReceiver();
        receiver.set(node, null, null);
        Assert.assertNotNull("The refactoring element should not be null", receiver.receiverNode);
    }

    public void testRenameMethodElementVisit() {
        RenameMethodElement element = new RenameMethodElement();
        RenameMethodReceiver receiver = mock(RenameMethodReceiver.class);
        element.accept(receiver);
        verify(receiver, times(1)).receive(element);
    }

    public void testRenameClassElementVisit() {
        RenameClassElement element = new RenameClassElement();
        RenameMethodReceiver receiver = mock(RenameMethodReceiver.class);
        element.accept(receiver);
        verify(receiver, times(1)).receive(element);
    }

    public void testRenameMethodRenameMethodOverrideConflict() {
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
        Node elementNode = new Node(renameParentFooMethod);
        Refactoring renameChildBarMethod = refactorings.get(2);
        Node visitorNode = new Node(renameChildBarMethod);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementNode, project);

        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = cell.renameMethodRenameMethodConflictCell(elementNode, visitorNode);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testRenameMethodRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Node elementNode = new Node(changeFirstOverloaded);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        Node visitorNode = new Node(changeSecondOverloaded);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementNode, project);
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = cell.renameMethodRenameMethodConflictCell(elementNode, visitorNode);
        Assert.assertTrue("Methods that start overloaded and get changed to different names should conflict", isConflicting);
    }

    public void testRenameMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(0);
        Node elementNode = new Node(elementRef);
        Refactoring visitorRef = refactorings.get(1);
        Node visitorNode = new Node(visitorRef);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementNode, project);
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = cell.renameMethodRenameMethodConflictCell(elementNode, visitorNode);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", isConflicting);
    }

    public void testRenameMethodRenameMethodNoConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        Node elementNode = new Node(elementRef);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementNode, project);
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = cell.renameMethodRenameMethodConflictCell(elementNode, elementNode);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", isConflicting);
    }

}
