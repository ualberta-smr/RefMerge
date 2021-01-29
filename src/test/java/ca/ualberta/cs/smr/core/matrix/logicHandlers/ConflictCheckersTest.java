package ca.ualberta.cs.smr.core.matrix.logicHandlers;

import ca.ualberta.cs.smr.GetDataForTests;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ConflictCheckersTest {

    @Test
    public void testCheckNamingConflict() {
        String originalElement = "foo";
        String originalVisitor = "bar";
        String refactoredElement = "newFoo";
        String refactoredVisitor = "newBar";
        String path = System.getProperty("user.dir");
        ConflictCheckers conflictCheckers = new ConflictCheckers(path);
        boolean expectedFalse = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings do not conflict", expectedFalse);

        originalVisitor = "foo";
        boolean expectedTrue = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because an element is renamed to two names", expectedTrue);
        refactoredVisitor = "newFoo";
        expectedFalse = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings are the same", expectedFalse);
        originalVisitor = "bar";
        expectedTrue = conflictCheckers.checkNamingConflict(originalElement, originalVisitor,
                                                                            refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because two elements are renamed to the same name", expectedTrue);
    }

    @Test
    public void testCheckMethodNamingConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MethodNamingConflict";
        String refactoredPath = basePath + "/src/test/resources/refactored/MethodNamingConflict";
        ConflictCheckers conflictCheckers = new ConflictCheckers(basePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        Refactoring visitorRef = refactorings.get(2);
        boolean expectedFalse = conflictCheckers.checkMethodNamingConflict(elementRef, visitorRef);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        visitorRef = refactorings.get(0);
        boolean expectedTrue = conflictCheckers.checkMethodNamingConflict(elementRef, visitorRef);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = conflictCheckers.checkMethodNamingConflict(visitorRef, elementRef);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = conflictCheckers.checkMethodNamingConflict(visitorRef, visitorRef);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", expectedFalse);
    }

    @Test
    public void testCheckClassNamingConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original/MatrixUtilsTests";
        String refactoredPath = basePath + "/src/test/resources/refactored/MatrixUtilsTests";
        ConflictCheckers conflictCheckers = new ConflictCheckers(basePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring element = refactorings.get(0);
        Refactoring visitor = refactorings.get(1);
        boolean expectedFalse = conflictCheckers.checkClassNamingConflict(element, visitor);
        Assert.assertFalse("Classes should not conflict", expectedFalse);

    }
}
