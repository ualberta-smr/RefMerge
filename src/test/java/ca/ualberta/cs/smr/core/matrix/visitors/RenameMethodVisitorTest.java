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

public class RenameMethodVisitorTest extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        visitor.set(node, null);
        Assert.assertNotNull("The refactoring element should not be null", visitor.visitorRef);
    }

    public void testRenameMethodElementVisit() {
        RenameMethodElement element = new RenameMethodElement();
        RenameMethodVisitor visitor = mock(RenameMethodVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
    }

    public void testRenameClassElementVisit() {
        RenameClassElement element = new RenameClassElement();
        RenameMethodVisitor visitor = mock(RenameMethodVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
    }

    public void testCheckRenameMethodConflictCall() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodElement element = mock(RenameMethodElement.class);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        Node node = new Node(ref);
        element.set(node, project);
        visitor.set(node, null);
        visitor.visit(element);
        verify(element, times(1)).checkRenameMethodConflict(ref);
    }
}
