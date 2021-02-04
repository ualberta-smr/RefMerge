package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class RenameMethodVisitorTest {

    @Test
    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverloadConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverloadConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        visitor.set(ref);
        Assert.assertNotNull("The refactoring element should not be null", visitor.visitorRef);
    }

    @Test
    public void testRenameMethodElementVisit() {
        RenameMethodElement element = new RenameMethodElement();
        RenameMethodVisitor visitor = mock(RenameMethodVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
    }

    @Test
    public void testRenameClassElementVisit() {
        RenameClassElement element = new RenameClassElement();
        RenameMethodVisitor visitor = mock(RenameMethodVisitor.class);
        element.accept(visitor);
        verify(visitor, times(1)).visit(element);
    }

    @Test
    public void testCheckRenameMethodConflictCall() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodNamingConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodNamingConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodElement element = mock(RenameMethodElement.class);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        element.set(ref, ref.getName());
        visitor.set(ref);
        visitor.visit(element);
        verify(element, times(1)).checkRenameMethodConflict(ref);
    }
}
