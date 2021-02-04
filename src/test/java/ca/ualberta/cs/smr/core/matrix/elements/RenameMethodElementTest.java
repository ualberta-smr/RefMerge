package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.logicHandlers.ConflictCheckers;
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

    @Test
    public void testCheckRenameMethodOverrideConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverrideConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverrideConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Refactoring renameChildBarMethod = refactorings.get(2);
        RenameMethodElement element = new RenameMethodElement();
        element.set(renameParentFooMethod, basePath);
        boolean isConflicting = element.checkRenameMethodConflict(renameChildBarMethod);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    @Test
    public void testCheckRenameMethodOverloadConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodOverloadConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodOverloadConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        RenameMethodElement element = new RenameMethodElement();
        element.set(changeFirstOverloaded, basePath);
        boolean isConflicting = element.checkRenameMethodConflict(changeSecondOverloaded);
        Assert.assertTrue("Methods that start overloaded and get changed to different names should conflict", isConflicting);
    }

    @Test
    public void testCheckRenameMethodNamingConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodNamingConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodNamingConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(0);
        Refactoring visitorRef = refactorings.get(1);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementRef, basePath);
        boolean isConflicting = element.checkRenameMethodConflict(visitorRef);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", isConflicting);
    }

    @Test
    public void testCheckRenameMethodNoConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodNamingConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodNamingConflict";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        RenameMethodElement element = new RenameMethodElement();
        element.set(elementRef, basePath);
        boolean isConflicting = element.checkRenameMethodConflict(elementRef);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", isConflicting);
    }

}
