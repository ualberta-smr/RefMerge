package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class RenameClassReceiverTest extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameClassReceiver receiver = new RenameClassReceiver();
        receiver.set(node, null, null);
        Assert.assertNotNull("The refactoring element should not be null", receiver.receiverNode);
    }

    public void testRenameMethodDispatcherReceive() {
        RenameClassDispatcher dispatcher = new RenameClassDispatcher();
        RenameMethodDispatcher wrongDispatcher = new RenameMethodDispatcher();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);

    }

    public void testRenameClassDispatcherReceive() {
        RenameMethodDispatcher dispatcher = new RenameMethodDispatcher();
        RenameClassDispatcher wrongDispatcher = new RenameClassDispatcher();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);
    }

    public void testRenameClassRenameClassConflictCell() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        RenameClassDispatcher renameClassDispatcher = new RenameClassDispatcher();
        Node leftNode = new Node(foo);
        Node rightNode = new Node(foo2);
        renameClassDispatcher.set(leftNode, project);
        boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(leftNode, rightNode);
        Assert.assertTrue(isConflicting);
        rightNode = new Node(bar);
        isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(leftNode, rightNode);
        Assert.assertFalse(isConflicting);
    }

    public void testRenameClassRenameMethodDependenceCell() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring rightRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring leftRef = classRefs.get(0);
        Node leftNode = new Node(leftRef);
        Node rightNode = new Node(rightRef);
        RenameClassDispatcher dispatcher = new RenameClassDispatcher();
        dispatcher.set(leftNode, project);
        boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(rightNode, leftNode);
        Assert.assertTrue(isDependent);
    }
}
