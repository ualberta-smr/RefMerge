package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.GetDataForTests;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.junit.Test;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

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
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        UMLOperation operation = MatrixUtils.getOriginalRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredRenameOperation() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        UMLOperation operation = MatrixUtils.getRefactoredRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                                        "checkIfClassroomWorks", name);
    }

    @Test
    public void testGetOriginalMethodName() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        String name = MatrixUtils.getOriginalMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", expectedName, name);
    }

    @Test
    public void testGetRefactoredMethodName() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        String name = MatrixUtils.getRefactoredMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                expectedName, name);
    }

    @Test
    public void testGetOriginalRenameOperationClassName() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        String name = MatrixUtils.getOriginalRenameOperationClassName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getClassName();
        Assert.assertEquals("The original class name of the rename refactoring should be \"Main\"",
                expectedName, name);
    }

    @Test
    public void testGetOriginalClassOperation() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        UMLClass umlClass = MatrixUtils.getOriginalClassOperation(refactoring);
        Assert.assertNotNull("The class should not be null", umlClass);
        String name = umlClass.getName();
        Assert.assertEquals("The name should be \"Child\"","Child", name);
    }

    @Test
    public void testGetRefactoredClassOperation() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        UMLClass umlClass = MatrixUtils.getRefactoredClassOperation(refactoring);
        Assert.assertNotNull("The class should not be null", umlClass);
        String name = umlClass.getName();
        Assert.assertEquals("The name should be \"ChildClass\"","ChildClass", name);
    }

    @Test
    public void testGetOriginalClassPackage() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassPackage(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getPackageName();
        Assert.assertEquals(name, expectedName);
    }

    @Test
    public void testGetOriginalClassOperationName() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getName();
        Assert.assertEquals("The original name of the class should be \"HelperFile\"", name, expectedName);

    }

    @Test
    public void testGetRefactoredClassOperationName() {
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS");
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getRefactoredClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getRenamedClass().getName();
        Assert.assertEquals("The original name of the class should be \"ClassFile\"", name, expectedName);
    }

    @Test
    public void testIfClassExtends() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/original";
        UMLClass elementParent = GetDataForTests.getClass(originalPath, "Main");
        UMLClass elementChild = GetDataForTests.getClass(originalPath, "Child");
        UMLClass elementOther = GetDataForTests.getClass(originalPath, "HelperFile");
        assert elementParent != null;
        assert elementChild != null;
        assert elementOther != null;
        boolean expectTrue = MatrixUtils.ifClassExtends(elementParent, elementChild);
        Assert.assertTrue("Child extends Parent", expectTrue);
        expectTrue = MatrixUtils.ifClassExtends(elementChild, elementParent);
        Assert.assertTrue("Child extends Parent, called in reverse order", expectTrue);
        boolean expectedFalse = MatrixUtils.ifClassExtends(elementParent, elementParent);
        Assert.assertFalse("Null check for super classes", expectedFalse);
        expectedFalse = MatrixUtils.ifClassExtends(elementChild, elementOther);
        Assert.assertFalse("Check for child extends another class", expectedFalse);
    }

}
