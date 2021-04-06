package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
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

    public void testRenameMethodElementVisit() {
        RenameClassElement element = new RenameClassElement();
        RenameMethodElement wrongElement = new RenameMethodElement();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        element.accept(receiver);
        verify(receiver, times(1)).receive(element);
        verify(receiver, never()).receive(wrongElement);

    }

    public void testRenameClassElementVisit() {
        RenameMethodElement element = new RenameMethodElement();
        RenameClassElement wrongElement = new RenameClassElement();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        element.accept(receiver);
        verify(receiver, times(1)).receive(element);
        verify(receiver, never()).receive(wrongElement);
    }

}
