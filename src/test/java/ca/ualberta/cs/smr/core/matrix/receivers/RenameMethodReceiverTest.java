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

public class RenameMethodReceiverTest extends LightJavaCodeInsightFixtureTestCase {

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

}
