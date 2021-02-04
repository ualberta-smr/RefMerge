package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.RefactoringType;

public class MatrixTest {

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
}
