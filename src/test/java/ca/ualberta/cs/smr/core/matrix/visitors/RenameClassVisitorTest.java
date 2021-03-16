package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class RenameClassVisitorTest extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameClassVisitor visitor = new RenameClassVisitor();
        visitor.set(node, null);
        Assert.assertNotNull("The refactoring element should not be null", visitor.visitorNode);
    }

    public void testRenameMethodElementVisit() {
        RenameClassElement element = new RenameClassElement();
        RenameMethodElement wrongElement = new RenameMethodElement();
        RenameClassVisitor visitor = mock(RenameClassVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
        verify(visitor, never()).visit(wrongElement);

    }

    public void testRenameClassElementVisit() {
        RenameMethodElement element = new RenameMethodElement();
        RenameClassElement wrongElement = new RenameClassElement();
        RenameClassVisitor visitor = mock(RenameClassVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
        verify(visitor, never()).visit(wrongElement);
    }

}
