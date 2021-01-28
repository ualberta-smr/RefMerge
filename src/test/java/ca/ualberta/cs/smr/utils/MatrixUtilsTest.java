package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.GetRefactoringsForTests;
import gr.uom.java.xmi.UMLOperation;
import org.junit.Test;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

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
        List<Refactoring> refs = GetRefactoringsForTests.getRefactorings();
        Refactoring refactoring = null;
        for(Refactoring ref : refs) {
            if (ref.getRefactoringType() == RefactoringType.RENAME_METHOD) {
                refactoring = ref;
            }
        }
        UMLOperation operation = MatrixUtils.getOriginalRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredRenameOperation() {
        List<Refactoring> refs = GetRefactoringsForTests.getRefactorings();
        Refactoring refactoring = null;
        for(Refactoring ref : refs) {
            if (ref.getRefactoringType() == RefactoringType.RENAME_METHOD) {
                refactoring = ref;
            }
        }
        UMLOperation operation = MatrixUtils.getRefactoredRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                                        "checkIfClassroomWorks", name);
    }

    @Test
    public void testGetOriginalMethodName() {
        List<Refactoring> refs = GetRefactoringsForTests.getRefactorings();
        Refactoring refactoring = null;
        for(Refactoring ref : refs) {
            if (ref.getRefactoringType() == RefactoringType.RENAME_METHOD) {
                refactoring = ref;
            }
        }
        String name = MatrixUtils.getOriginalMethodName(refactoring);
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredMethodName() {
        List<Refactoring> refs = GetRefactoringsForTests.getRefactorings();
        Refactoring refactoring = null;
        for(Refactoring ref : refs) {
            if (ref.getRefactoringType() == RefactoringType.RENAME_METHOD) {
                refactoring = ref;
            }
        }
        String name = MatrixUtils.getRefactoredMethodName(refactoring);
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                "checkIfClassroomWorks", name);
    }
}
