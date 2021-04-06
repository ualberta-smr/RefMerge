package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameMethodRenameMethodCell;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.times;

public class RenameMethodElementTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testAccept() {
        RenameMethodElement element = Mockito.mock(RenameMethodElement.class);
        RenameMethodReceiver visitor = new RenameMethodReceiver();
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    public void testSet() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        Assert.assertNotNull("The refactoring element should not be null", element.elementNode);

    }

}
