package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;


import java.util.List;

import static org.mockito.Mockito.times;

public class RenameClassElementTest extends LightJavaCodeInsightFixtureTestCase {

    public void testAccept() {
        RenameClassElement element = Mockito.mock(RenameClassElement.class);
        RenameMethodReceiver visitor = new RenameMethodReceiver();
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    public void testSet() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameClassElement element = new RenameClassElement();
        element.set(node, project);
        Assert.assertNotNull("The refactoring element should not be null", element.elementNode);
    }

    public void testCheckRenameClassConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        RenameClassElement renameClassElement = new RenameClassElement();
        Node elementNode = new Node(foo);
        Node visitorNode = new Node(foo2);
        renameClassElement.set(elementNode, project);
        boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(elementNode, visitorNode);
        Assert.assertTrue(isConflicting);
        visitorNode = new Node(bar);
        isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(elementNode, visitorNode);
        Assert.assertFalse(isConflicting);
    }

    public void testCheckRenameMethodDependence() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring visitorRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring elementRef = classRefs.get(0);
        Node elementNode = new Node(elementRef);
        Node visitorNode = new Node(visitorRef);
        RenameClassElement element = new RenameClassElement();
        element.set(elementNode, project);
        boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(elementNode, visitorNode);
        Assert.assertTrue(isDependent);
    }
}
