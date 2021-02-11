package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        UMLOperation operation = MatrixUtils.getOriginalRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredRenameOperation() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);

        String name = MatrixUtils.getOriginalMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", expectedName, name);
    }

    @Test
    public void testGetRefactoredMethodName() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
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
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassPackage(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getPackageName();
        Assert.assertEquals(name, expectedName);
    }

    @Test
    public void testGetOriginalClassOperationName() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getName();
        Assert.assertEquals("The original name of the class should be \"HelperFile\"", name, expectedName);

    }

    @Test
    public void testGetRefactoredClassOperationName() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String refactoredPath = basePath + "/src/test/resources/matrixUtilsTests/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring refactoring = refactorings.get(0);
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getRefactoredClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getRenamedClass().getName();
        Assert.assertEquals("The original name of the class should be \"ClassFile\"", name, expectedName);
    }

    @Test
    public void testGetUMLClass() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String className = "Main";
        UMLClass umlClass = MatrixUtils.getUMLClass(className, originalPath);
        Assert.assertNotNull("The UML class should not be null", umlClass);
        Assert.assertEquals("The name of the UML class should be \"Main\"", umlClass.getName(), className);
        umlClass = MatrixUtils.getUMLClass("", "");
        Assert.assertNull("getUMLClass should return null", umlClass);
    }

    @Test
    public void testIfClassExtends() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
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
        expectedFalse = MatrixUtils.ifClassExtends(elementOther, elementChild);
        Assert.assertFalse("Check for child extends another class, called in reverse order", expectedFalse);
    }

}
