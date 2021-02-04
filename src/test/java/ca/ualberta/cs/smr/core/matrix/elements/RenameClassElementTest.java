package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;


import java.util.List;

import static org.mockito.Mockito.times;

public class RenameClassElementTest {

    @Test
    public void testAccept() {
        RenameClassElement element = Mockito.mock(RenameClassElement.class);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    @Test
    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/RenameClassConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/RenameClassConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameClassElement element = new RenameClassElement();
        element.set(ref, basePath);
        Assert.assertNotNull("The refactoring element should not be null", element.elementRef);
        Assert.assertEquals("The set path should be the same as the base path", element.path, basePath);
    }

    @Test
    public void testCheckRenameClassConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/RenameClassConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/RenameClassConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        RenameClassElement renameClassElement = new RenameClassElement();
        renameClassElement.set(foo, basePath);
        boolean isConflicting = renameClassElement.checkRenameClassConflict(foo2);
        Assert.assertTrue(isConflicting);
        isConflicting = renameClassElement.checkRenameClassConflict(bar);
        Assert.assertFalse(isConflicting);
    }
}
