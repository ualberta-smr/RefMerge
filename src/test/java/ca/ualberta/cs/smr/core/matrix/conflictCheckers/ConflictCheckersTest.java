package ca.ualberta.cs.smr.core.matrix.conflictCheckers;

import ca.ualberta.cs.smr.GetDataForTests;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ConflictCheckersTest {

    @Test
    public void testSet() {
        String path = "testPath";
        ConflictCheckers conflictCheckers = new ConflictCheckers(path);
        Assert.assertEquals(path, conflictCheckers.path);

    }

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
    public void testCheckOverrideConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(basePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Refactoring renameOtherFooMethod = refactorings.get(1);
        Refactoring renameChildBarMethod = refactorings.get(2);
        Refactoring renameOtherBarMethod = refactorings.get(3);
        Refactoring renameFooBarMethod = refactorings.get(4);
        boolean isConflicting = conflictCheckers.checkOverrideConflict(renameParentFooMethod, renameOtherFooMethod);
        Assert.assertFalse("Renamings in the same class should not result in override conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(renameOtherFooMethod, renameChildBarMethod);
        Assert.assertTrue("Methods that do not override but override after refactoring should conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(renameParentFooMethod, renameOtherBarMethod);
        Assert.assertFalse("Methods that have no override relation, before or after, should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(renameParentFooMethod, renameFooBarMethod);
        Assert.assertFalse("Classes that have no inheritance should not result in override conflicts", isConflicting);
        isConflicting = conflictCheckers.checkOverrideConflict(renameParentFooMethod, renameChildBarMethod);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    @Test
    public void testCheckOverloadConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(basePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        Refactoring changeOtherMethodToSecondOverloaded = refactorings.get(2);
        Refactoring changeOtherBarMethod = refactorings.get(3);
        boolean isConflicting = conflictCheckers.checkOverloadConflict(changeFirstOverloaded, changeOtherBarMethod);
        Assert.assertFalse("Methods in different classes should not cause overload conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverloadConflict(changeFirstOverloaded, changeOtherMethodToSecondOverloaded);
        Assert.assertFalse("Methods in the same class that do not have related names " +
                "before or after being refactored should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkOverloadConflict(changeSecondOverloaded, changeOtherMethodToSecondOverloaded);
        Assert.assertTrue("Methods that overload after refactoring should conflict", isConflicting);
    }

    @Test
    public void testCheckMethodNamingConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
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
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        ConflictCheckers conflictCheckers = new ConflictCheckers(basePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        boolean isConflicting = conflictCheckers.checkClassNamingConflict(foo, bar);
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        isConflicting = conflictCheckers.checkClassNamingConflict(foo, foo2);
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }
}
