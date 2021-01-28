package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.Matrix;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiClass;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.jdom.JDOMException;
import org.junit.Test;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.io.IOException;
import java.lang.reflect.Method;
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
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_METHOD");

        UMLOperation operation = MatrixUtils.getOriginalRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", "doStuff", name);
    }

    @Test
    public void testGetRefactoredRenameOperation() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_METHOD");

        UMLOperation operation = MatrixUtils.getRefactoredRenameOperation(refactoring);
        Assert.assertNotNull("The refactoring operation should not be null", operation);
        String name = operation.getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                                        "checkIfClassroomWorks", name);
    }

    @Test
    public void testGetOriginalMethodName() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getOriginalMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
        Assert.assertEquals("The original name of the rename refactoring should be \"doStuff\"", expectedName, name);
    }

    @Test
    public void testGetRefactoredMethodName() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getRefactoredMethodName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
        Assert.assertEquals("The refactored name of the rename refactoring should be \"checkIfClassroomWorks\"",
                expectedName, name);
    }

    @Test
    public void testGetOriginalRenameOperationClassName() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_METHOD");

        String name = MatrixUtils.getOriginalRenameOperationClassName(refactoring);
        assert refactoring != null;
        String expectedName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getClassName();
        Assert.assertEquals("The original class name of the rename refactoring should be \"Main\"",
                expectedName, name);
    }

    @Test
    public void testGetOriginalClassOperation() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_CLASS");
        Assert.assertNotNull(refactoring);
        UMLClass umlClass = MatrixUtils.getOriginalClassOperation(refactoring);
        Assert.assertNotNull("The class should not be null", umlClass);
        String name = umlClass.getName();
        Assert.assertEquals("The name should be \"HelperFile\"","HelperFile", name);
    }

    @Test
    public void testGetRefactoredClassOperation() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_CLASS");
        Assert.assertNotNull(refactoring);
        UMLClass umlClass = MatrixUtils.getRefactoredClassOperation(refactoring);
        Assert.assertNotNull("The class should not be null", umlClass);
        String name = umlClass.getName();
        Assert.assertEquals("The name should be \"ClassFile\"","ClassFile", name);
    }

    @Test
    public void testGetOriginalClassPackage() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_CLASS");
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassPackage(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getPackageName();
        Assert.assertEquals(name, expectedName);
    }

    @Test
    public void testGetOriginalClassOperationName() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_CLASS");
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getOriginalClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getOriginalClass().getName();
        Assert.assertEquals("The original name of the class should be \"HelperFile\"", name, expectedName);

    }

    @Test
    public void testGetRefactoredClassOperationName() {
        Refactoring refactoring = GetDataForTests.getRefactorings("RENAME_CLASS");
        Assert.assertNotNull(refactoring);
        String name = MatrixUtils.getRefactoredClassOperationName(refactoring);
        String expectedName = ((RenameClassRefactoring) refactoring).getRenamedClass().getName();
        Assert.assertEquals("The original name of the class should be \"ClassFile\"", name, expectedName);
    }

}
