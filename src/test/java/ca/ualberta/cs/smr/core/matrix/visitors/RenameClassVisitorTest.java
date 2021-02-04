package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.times;

public class RenameClassVisitorTest {
    @Test
    public void testVisit() {
        RenameMethodVisitor visitor = Mockito.mock(RenameMethodVisitor.class);
        RenameClassElement element = new RenameClassElement();
        element.accept(visitor);
        Mockito.verify(visitor, times(1)).visit(element);
    }

    @Test
    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/RenameClassConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/RenameClassConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameClassVisitor visitor = new RenameClassVisitor();
        visitor.set(ref);
        Assert.assertNotNull("The refactoring element should not be null", visitor.visitorRef);
    }
}
