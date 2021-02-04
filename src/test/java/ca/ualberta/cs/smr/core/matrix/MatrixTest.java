package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

public class MatrixTest {

    @Test
    public void testMatrixConstructor() {
        Matrix matrix = new Matrix("Path");
        Assert.assertEquals("Path", matrix.path);
    }

    @Test
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

    @Test
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

    @Test
    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverloadConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverloadConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodElement mockElement = new RenameMethodElement();
        Matrix matrix = new Matrix(basePath);
        RefactoringElement element = matrix.makeElement(ref.getRefactoringType(), ref);
        boolean equals = element.getClass().equals(mockElement.getClass());
        Assert.assertTrue(equals);


    }

    @Test
    public void testMakeVisitor() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverloadConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverloadConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodVisitor mockVisitor = new RenameMethodVisitor();
        Matrix matrix = new Matrix(basePath);
        RefactoringVisitor visitor = matrix.makeVisitor(ref.getRefactoringType(), ref);
        boolean equals = visitor.getClass().equals(mockVisitor.getClass());
        Assert.assertTrue(equals);
    }
}
