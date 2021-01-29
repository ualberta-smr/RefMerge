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
    public void testCheckClassNamingConflict() {
        String path = System.getProperty("user.dir");
        ConflictCheckers conflictCheckers = new ConflictCheckers(path);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        Refactoring element = refactorings.get(0);
        Refactoring visitor = refactorings.get(1);
        boolean expectedFalse = conflictCheckers.checkClassNamingConflict(element, visitor);
        Assert.assertFalse("Classes should not conflict", expectedFalse);

    }
}
