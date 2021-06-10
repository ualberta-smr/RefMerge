package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.core.refactoringObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.ParameterObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class RenameMethodRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckRenameMethodRenameMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Refactoring renameOtherFooMethod = refactorings.get(1);
        Refactoring renameChildBarMethod = refactorings.get(2);
        Refactoring renameOtherBarMethod = refactorings.get(3);
        Refactoring renameFooBarMethod = refactorings.get(4);
        RefactoringObject leftRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameParentFooMethod);
        RefactoringObject rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameOtherFooMethod);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Renamings in the same class should not result in override conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameOtherBarMethod);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Methods that have no override relation, before or after, should not conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameFooBarMethod);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Classes that have no inheritance should not result in override conflicts", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameChildBarMethod);
        isConflicting = renameMethodRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testCheckRenameMethodRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 3;
        Refactoring leftRefactoring = refactorings.get(0);
        Refactoring rightRefactoring = refactorings.get(2);
        RefactoringObject leftRefactoringObject = RefactoringObjectUtils.createRefactoringObject(leftRefactoring);
        RefactoringObject rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Methods in the same class that do not have related names " +
                "before or after being refactored should not conflict", isConflicting);

        List<ParameterObject> leftParameters = new ArrayList<>();
        leftParameters.add(new ParameterObject("int", "return"));
        MethodSignatureObject foo = new MethodSignatureObject(leftParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(leftParameters, "bar");
        List<ParameterObject> rightParameters = new ArrayList<>();
        rightParameters.add(new ParameterObject("int", "return"));
        rightParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foobar = new MethodSignatureObject(rightParameters, "foobar");
        MethodSignatureObject bar2 = new MethodSignatureObject(rightParameters, "bar");
        // A.foo -> A.bar
        leftRefactoringObject = new RenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        // A.foobar(x) -> A.bar(x)
        rightRefactoringObject = new RenameMethodObject("A.java", "A",
                foobar, "A.java", "A", bar2);
        isConflicting = renameMethodRenameMethodCell.checkOverloadConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Methods in the same class that are renamed to the same name " +
                "with different signatures should be an accidental overload", isConflicting);
    }



    public void testCheckRenameMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring leftRefactoring = refactorings.get(1);
        Refactoring rightRefactoring = refactorings.get(2);
        RefactoringObject leftRefactoringObject = RefactoringObjectUtils.createRefactoringObject(leftRefactoring);
        RefactoringObject rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        rightRefactoring = refactorings.get(0);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring);
        boolean expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = renameMethodRenameMethodCell.checkMethodNamingConflict(rightRefactoringObject, leftRefactoringObject);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = renameMethodRenameMethodCell.checkMethodNamingConflict(rightRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", expectedFalse);
    }

    public void testNestedRenameMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> methodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefactorings != null;
        assert classRefactorings != null;
        Refactoring dispatcherRef = methodRefactorings.get(0);
        Refactoring rightRef = methodRefactorings.get(3);
        Refactoring classRef = classRefactorings.get(0);

        ArrayList<RefactoringObject> rightList = new ArrayList<>();
        RefactoringObject rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRef);
        RefactoringObject rightClassRenameObject = RefactoringObjectUtils.createRefactoringObject(classRef);
        RefactoringObject leftRefactoringObject = RefactoringObjectUtils.createRefactoringObject(dispatcherRef);
        Matrix matrix = new Matrix(null);
        matrix.simplifyAndInsertRefactorings(rightClassRenameObject,rightList);
        matrix.simplifyAndInsertRefactorings(rightRefactoringObject,rightList);
        rightRefactoringObject = rightList.get(1);
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = renameMethodRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue(isConflicting);
    }

    public void testFoundRenameMethodRenameMethodTransitivity() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        MethodSignatureObject foobar = new MethodSignatureObject(originalParameters, "foobar");
        // Rename method A.foo -> A.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        // Rename method A.bar -> A.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("A.java", "A",
                bar, "A.java", "A", foobar);
        // Rename Method A.foo -> A.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                foo, "A.java", "A", foobar);

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testFoundRenameMethodRenameMethodTransitivity2() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        MethodSignatureObject foobar = new MethodSignatureObject(originalParameters, "foobar");
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        // Rename method B.bar -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("B.java", "B",
                bar, "C.java", "C", foobar);
        // Rename Method A.foo -> C.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                foo, "C.java", "C", foobar);

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testNotFoundRenameMethodRenameMethodTransitivity() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        MethodSignatureObject buzz = new MethodSignatureObject(originalParameters, "buzz");
        MethodSignatureObject foobar = new MethodSignatureObject(originalParameters, "foobar");
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        // Rename method B.buzz -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject("B.java", "B",
                buzz, "C.java", "C", foobar);
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedRefactoring = new RenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }

    private void doRenameMethodRenameMethodTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(null);
        boolean isTransitive = cell.checkRenameMethodRenameMethodTransitivity(firstRefactoring, secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
        }
        else {
            Assert.assertFalse(isTransitive);
        }
        MethodSignatureObject firstOriginalSignature = ((RenameMethodObject) firstRefactoring).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((RenameMethodObject) expectedRefactoring).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((RenameMethodObject) firstRefactoring).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((RenameMethodObject) expectedRefactoring).getDestinationMethodSignature();
        MethodSignatureObject secondOriginalSignature = ((RenameMethodObject) secondRefactoring).getOriginalMethodSignature();
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationClassName(),
                ((RenameMethodObject) firstRefactoring).getDestinationClassName());
        Assert.assertFalse(expectedOriginalSignature.equalsSignature(secondOriginalSignature));
        Assert.assertTrue(firstOriginalSignature.equalsSignature(expectedOriginalSignature));
        Assert.assertTrue(firstDestinationSignature.equalsSignature(expectedDestinationSignature));
    }
}
