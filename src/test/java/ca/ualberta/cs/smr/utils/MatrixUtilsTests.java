package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.refmerge.utils.MatrixUtils;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class MatrixUtilsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testIsSameName() {
        String foo = "foo";
        String foo2 = "foo";
        String bar = "bar";
        boolean expectedFalse = MatrixUtils.isSameName(foo, bar);
        Assert.assertFalse("isSameName returned true when it should have returned false", expectedFalse);
        boolean expectedTrue = MatrixUtils.isSameName(foo, foo2);
        Assert.assertTrue("isSameName returned false when it should have returned true", expectedTrue);
    }

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

    public void testIfClassExtends() {

        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String filePath = "matrixUtilsTests/original";
        PsiFile[] psiFiles = myFixture.configureByFiles(filePath + "/Main.java", filePath + "/HelperFile.java");

        UMLClass elementParent = GetDataForTests.getClass(originalPath, "Main");
        UMLClass elementChild = GetDataForTests.getClass(originalPath, "Child");
        UMLClass elementOther = GetDataForTests.getClass(originalPath, "HelperFile");
        assert elementParent != null;
        assert elementChild != null;
        assert elementOther != null;

        PsiClass[] psiClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        PsiClass psiParent = TestUtils.findPsiClassFromUML(elementParent, psiClasses);
        PsiClass psiChild = TestUtils.findPsiClassFromUML(elementChild, psiClasses);
        psiClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);
        PsiClass psiOther = TestUtils.findPsiClassFromUML(elementOther, psiClasses);

        assert psiParent != null;
        assert psiChild != null;
        assert psiOther != null;
        boolean expectTrue = MatrixUtils.ifClassExtends(psiParent, psiChild);
        Assert.assertTrue("Child extends Parent", expectTrue);
        expectTrue = MatrixUtils.ifClassExtends(psiChild, psiParent);
        Assert.assertTrue("Child extends Parent, called in reverse order", expectTrue);
        boolean expectedFalse = MatrixUtils.ifClassExtends(psiParent, psiParent);
        Assert.assertFalse("Null check for super classes", expectedFalse);
        expectedFalse = MatrixUtils.ifClassExtends(psiChild, psiOther);
        Assert.assertFalse("Check for child extends another class", expectedFalse);
        expectedFalse = MatrixUtils.ifClassExtends(psiOther, psiChild);
        Assert.assertFalse("Check for child extends another class, called in reverse order", expectedFalse);
    }

    public void testIfClassExtendsHierarchy() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/matrixUtilsTests/original";
        String filePath = "matrixUtilsTests/original";
        PsiFile[] psiFiles = myFixture.configureByFiles(filePath + "/Main.java");

        UMLClass elementParent = GetDataForTests.getClass(originalPath, "Main");
        UMLClass elementChild = GetDataForTests.getClass(originalPath, "ChildsChild");
        assert elementParent != null;
        assert elementChild != null;

        PsiClass[] psiClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        PsiClass parent = TestUtils.findPsiClassFromUML(elementParent, psiClasses);
        PsiClass child = TestUtils.findPsiClassFromUML(elementChild, psiClasses);

        assert parent != null;
        assert child != null;
        boolean expectTrue = MatrixUtils.ifClassExtends(parent, child);
        Assert.assertTrue("Child extends Parent through additional inheritance level", expectTrue);
    }

    public void testCheckNamingConflict() {
        String originalElement = "foo";
        String originalVisitor = "bar";
        String refactoredElement = "newFoo";
        String refactoredVisitor = "newBar";
        boolean expectedFalse = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings do not conflict", expectedFalse);

        originalVisitor = "foo";
        boolean expectedTrue = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because an element is renamed to two names", expectedTrue);
        refactoredVisitor = "newFoo";
        expectedFalse = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                refactoredElement, refactoredVisitor);
        Assert.assertFalse("Expected false because the renamings are the same", expectedFalse);
        originalVisitor = "bar";
        expectedTrue = MatrixUtils.checkNamingConflict(originalElement, originalVisitor,
                refactoredElement, refactoredVisitor);
        Assert.assertTrue("Expected true because two elements are renamed to the same name", expectedTrue);
    }


}
