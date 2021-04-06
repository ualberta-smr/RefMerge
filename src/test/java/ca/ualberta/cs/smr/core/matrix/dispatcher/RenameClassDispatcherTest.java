package ca.ualberta.cs.smr.core.matrix.dispatcher;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;


import java.util.List;

import static org.mockito.Mockito.times;

public class RenameClassDispatcherTest extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameClassDispatcher dispatcher = new RenameClassDispatcher();
        dispatcher.set(node, project);
        Assert.assertNotNull("The refactoring dispatcher should not be null", dispatcher.dispatcherNode);
    }

}
