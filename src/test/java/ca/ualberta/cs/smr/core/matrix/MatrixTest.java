package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RefactoringDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameClassReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

public class MatrixTest extends LightJavaCodeInsightFixtureTestCase {

    public void testElementMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassDispatcher renameClassElement = new RenameClassDispatcher();
        RenameMethodDispatcher renameMethodElement = new RenameMethodDispatcher();
        RefactoringDispatcher element = Matrix.dispatcherMap.get(type);
        boolean equals = element.getClass().equals(renameClassElement.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        element = Matrix.dispatcherMap.get(type);
        equals = element.getClass().equals(renameMethodElement.getClass());
        Assert.assertTrue(equals);
    }

    public void testReceiverMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassReceiver renameClassReceiver = new RenameClassReceiver();
        RenameMethodReceiver renameMethodReceiver = new RenameMethodReceiver();
        Receiver receiver = Matrix.receiverMap.get(type);
        boolean equals = receiver.getClass().equals(renameClassReceiver.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        receiver = Matrix.receiverMap.get(type);
        equals = receiver.getClass().equals(renameMethodReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0).getValue();
        Node node = new Node(ref);
        RenameMethodDispatcher mockElement = new RenameMethodDispatcher();
        Matrix matrix = new Matrix(null);
        RefactoringDispatcher element = matrix.makeDispatcher(node);
        boolean equals = element.getClass().equals(mockElement.getClass());
        Assert.assertTrue(equals);


    }

    public void testMakeReceiver() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodReceiver mockReceiver = new RenameMethodReceiver();
        Matrix matrix = new Matrix(null);
        Receiver receiver = matrix.makeReceiver(node);
        boolean equals = receiver.getClass().equals(mockReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testGetRefactoringValue() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String renamedPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, renamedPath);
        assert refactorings != null;
        Refactoring renameMethod = refactorings.get(1).getValue();
        originalPath = basePath + "/src/test/resources/extractTestData/extractMethod/original/";
        String extractedPath = basePath + "/src/test/resources/extractTestData/extractMethod/refactored/";
        refactorings = GetDataForTests.getPairs("EXTRACT_OPERATION", originalPath, extractedPath);
        assert refactorings != null;
        Refactoring extractMethod = refactorings.get(0).getValue();
        Node renameNode = new Node(renameMethod);
        Node extractNode = new Node(extractMethod);
        DependenceGraph graph = new DependenceGraph(project);
        graph.addVertex(renameNode);
        graph.addVertex(extractNode);
        Matrix matrix = new Matrix(project, graph);
        int renameValue = matrix.getRefactoringValue(renameMethod.getRefactoringType());
        int extractValue = matrix.getRefactoringValue(extractMethod.getRefactoringType());
        Assert.assertTrue(renameValue < extractValue);

    }


}
