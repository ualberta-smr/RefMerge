package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.times;

public class RenameMethodElementTest {

    @Test
    public void testAccept() {
        RenameMethodElement element = Mockito.mock(RenameMethodElement.class);
        RenameMethodVisitor visitor = Mockito.mock(RenameMethodVisitor.class);
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    @Test
    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverloadConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverloadConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodElement element = new RenameMethodElement();
        element.set(ref, basePath);
        Assert.assertNotNull("The refactoring element should not be null", element.elementRef);
        Assert.assertEquals("The set path should be the same as the base path", element.path, basePath);

    }
}
