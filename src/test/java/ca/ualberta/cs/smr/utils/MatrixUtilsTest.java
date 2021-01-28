package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.GetRefactoringsForTests;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.junit.Test;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

public class MatrixUtilsTest {

    @Test
    public void testIsSameName() {
        String foo = "foo";
        String foo2 = "foo";
        String bar = "bar";
        boolean expectedFalse = MatrixUtils.isSameName(foo, bar);
        Assert.assertFalse("isSameName returned true when it should have returned false", expectedFalse);
        boolean expectedTrue = MatrixUtils.isSameName(foo, foo2);
        Assert.assertTrue("isSameName returned false when it should have returned true", expectedTrue);
    }

    @Test
    public void testGetOriginalRenameOperation() {
        Refactoring refactoring = GetRefactoringsForTests.getRefactorings("RENAME_METHOD");

        UMLOperation operation = MatrixUtils.getOriginalRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredRenameOperation() {
        Refactoring refactoring = GetRefactoringsForTests.getRefactorings("RENAME_METHOD");

        UMLOperation operation = MatrixUtils.getRefactoredRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                                        "checkIfClassroomWorks", name);
    }

    @Test
    public void testGetOriginalMethodName() {
        Refactoring refactoring = GetRefactoringsForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getOriginalMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", expectedName, name);
    }

    @Test
    public void testGetRefactoredMethodName() {
        Refactoring refactoring = GetRefactoringsForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getRefactoredMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                expectedName, name);
    }

    @Test
    public void testGetOriginalRenameOperationClassName() {
        Refactoring refactoring = GetRefactoringsForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getOriginalRenameOperationClassName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getClassName();
        Assert.assertEquals("The original class name of the rename refactoring should be \"Main\"",
                expectedName, name);
    }
}
