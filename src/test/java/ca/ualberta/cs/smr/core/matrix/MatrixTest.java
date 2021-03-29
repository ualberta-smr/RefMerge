package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
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
        RenameClassElement renameClassElement = new RenameClassElement();
        RenameMethodElement renameMethodElement = new RenameMethodElement();
        RefactoringElement element = Matrix.elementMap.get(type);
        boolean equals = element.getClass().equals(renameClassElement.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        element = Matrix.elementMap.get(type);
        equals = element.getClass().equals(renameMethodElement.getClass());
        Assert.assertTrue(equals);
    }

    public void testVisitorMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassVisitor renameClassVisitor = new RenameClassVisitor();
        RenameMethodVisitor renameMethodVisitor = new RenameMethodVisitor();
        RefactoringVisitor visitor = Matrix.visitorMap.get(type);
        boolean equals = visitor.getClass().equals(renameClassVisitor.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        visitor = Matrix.visitorMap.get(type);
        equals = visitor.getClass().equals(renameMethodVisitor.getClass());
        Assert.assertTrue(equals);
    }

    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0).getValue();
        Node node = new Node(ref);
        RenameMethodElement mockElement = new RenameMethodElement();
        Matrix matrix = new Matrix(null);
        RefactoringElement element = matrix.makeElement(node);
        boolean equals = element.getClass().equals(mockElement.getClass());
        Assert.assertTrue(equals);


    }

    public void testMakeVisitor() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodVisitor mockVisitor = new RenameMethodVisitor();
        Matrix matrix = new Matrix(null);
        RefactoringVisitor visitor = matrix.makeVisitor(node);
        boolean equals = visitor.getClass().equals(mockVisitor.getClass());
        Assert.assertTrue(equals);
    }

    public void testGetRefactoringValue() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String renamedPath = basePath + "/src/test/testData/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, renamedPath);
        assert refactorings != null;
        Refactoring renameMethod = refactorings.get(1).getValue();
        originalPath = basePath + "/src/test/testData/extractTestData/extractMethod/original/";
        String extractedPath = basePath + "/src/test/testData/extractTestData/extractMethod/refactored/";
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
