package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.matrix.Matrix;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class MoveRenameMethodMoveRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

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
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Renamings in the same class should not result in override conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameOtherBarMethod);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Methods that have no override relation, before or after, should not conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameFooBarMethod);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Classes that have no inheritance should not result in override conflicts", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(renameChildBarMethod);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverrideConflict(leftRefactoringObject, rightRefactoringObject);
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
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverloadConflict(leftRefactoringObject, rightRefactoringObject);
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
        leftRefactoringObject = new MoveRenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        // A.foobar(x) -> A.bar(x)
        rightRefactoringObject = new MoveRenameMethodObject("A.java", "A",
                foobar, "A.java", "A", bar2);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkOverloadConflict(leftRefactoringObject, rightRefactoringObject);
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
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean expectedFalse = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Methods in different classes should not have naming conflicts", expectedFalse);
        rightRefactoring = refactorings.get(0);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring);
        boolean expectedTrue = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", expectedTrue);
        expectedTrue = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(rightRefactoringObject, leftRefactoringObject);
        Assert.assertTrue("The same refactorings in a different order should return true", expectedTrue);
        expectedFalse = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(rightRefactoringObject, rightRefactoringObject);
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
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue(isConflicting);
    }

    public void testNestedRenameMethodRenameMethodNamingConflict2() {
        Project project = myFixture.getProject();

        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("void", "return"));
        MethodSignatureObject sumNumbers = new MethodSignatureObject(originalParameters, "sumNumbers");
        MethodSignatureObject subNumbers = new MethodSignatureObject(originalParameters, "subNumbers");
        MethodSignatureObject numbers = new MethodSignatureObject(originalParameters, "numbers");
        // Rename method Foo.sumNumbers -> Fuzz.sumNumbers
        MoveRenameMethodObject moveMethodObject = new MoveRenameMethodObject("Classes.java", "Foo",
                sumNumbers, "Classes.java", "Fuzz", sumNumbers);
        moveMethodObject.setType(RefactoringType.MOVE_OPERATION);
        // Rename method Foo.sumNumbers -> Foo.numbers
        MoveRenameMethodObject renameMethodObject = new MoveRenameMethodObject("Classes.java", "Foo",
                numbers, "Classes.java", "Foo", sumNumbers);
        renameMethodObject.setType(RefactoringType.RENAME_METHOD);
        // Rename method Foo.sumNumbers -> Foo.subNumbers
        MoveRenameMethodObject renameMethodObject2 = new MoveRenameMethodObject("Classes.java", "Foo",
                numbers, "Classes.java", "Foo", subNumbers);
        renameMethodObject2.setType(RefactoringType.RENAME_METHOD);
        // Rename method Foo.sumNumbers -> Foo.subNumbers
        MoveRenameMethodObject renameMethodObject3 = new MoveRenameMethodObject("Classes.java", "Foo",
                sumNumbers, "Classes.java", "Foo", subNumbers);
        renameMethodObject3.setType(RefactoringType.RENAME_METHOD);
        // Rename and move method Foo.numbers -> Fuzz.sumNumbers
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("Classes.java", "Foo",
                numbers, "Classes.java", "Fuzz", sumNumbers);
        moveRenameMethodObject.setType(RefactoringType.MOVE_OPERATION);
        moveRenameMethodObject.setType(RefactoringType.RENAME_METHOD);
        MoveRenameMethodMoveRenameMethodCell moveRenameMethodMoveRenameMethodCell = new MoveRenameMethodMoveRenameMethodCell(project);
        boolean isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(moveMethodObject, moveRenameMethodObject);
        Assert.assertTrue(isConflicting);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(renameMethodObject, moveRenameMethodObject);
        Assert.assertTrue(isConflicting);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(renameMethodObject, renameMethodObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(renameMethodObject3, renameMethodObject2);
        Assert.assertTrue(isConflicting);
        isConflicting = moveRenameMethodMoveRenameMethodCell.checkMethodNamingConflict(renameMethodObject, renameMethodObject3);
        Assert.assertFalse(isConflicting);

    }


    public void testMoveRenameMethodDependence() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Rename method A.foo -> A.bar
        MoveRenameMethodObject renameMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        renameMethodObject.setType(RefactoringType.RENAME_METHOD);
        // Move method A.foo -> B.foo
        MoveRenameMethodObject moveMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", foo);
        moveMethodObject.setType(RefactoringType.MOVE_OPERATION);
        // Move method A.foo -> C.foo
        MoveRenameMethodObject moveMethodObject2 = new MoveRenameMethodObject("A.java", "A",
                foo, "C.java", "B", foo);
        moveMethodObject2.setType(RefactoringType.MOVE_OPERATION);
        // Move and rename method A.foo -> B.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        moveRenameMethodObject.setType(RefactoringType.RENAME_METHOD);
        moveRenameMethodObject.setType(RefactoringType.MOVE_OPERATION);
        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(getProject());
        // A.foo -> A.bar / A.foo -> B.foo
        boolean isDependent = cell.checkDependence(renameMethodObject, moveMethodObject);
        Assert.assertTrue(isDependent);
        // A.foo -> B.foo / A.foo -> C.foo
        isDependent = cell.checkDependence(moveMethodObject, moveMethodObject2);
        Assert.assertFalse(isDependent);
        // A.foo -> B.foo / A.foo -> B.bar
        isDependent = cell.checkDependence(moveMethodObject, moveRenameMethodObject);
        Assert.assertFalse(isDependent);
    }

    public void testFoundRenameMethodRenameMethodTransitivity() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        MethodSignatureObject foobar = new MethodSignatureObject(originalParameters, "foobar");
        // Rename method A.foo -> A.bar
        MoveRenameMethodObject firstRefactoring = new MoveRenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        // Rename method A.bar -> A.foobar
        MoveRenameMethodObject secondRefactoring = new MoveRenameMethodObject("A.java", "A",
                bar, "A.java", "A", foobar);
        // Rename Method A.foo -> A.foobar
        MoveRenameMethodObject expectedRefactoring = new MoveRenameMethodObject("A.java", "A",
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
        MoveRenameMethodObject firstRefactoring = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        // Rename method B.bar -> C.foobar
        MoveRenameMethodObject secondRefactoring = new MoveRenameMethodObject("B.java", "B",
                bar, "C.java", "C", foobar);
        // Rename Method A.foo -> C.foobar
        MoveRenameMethodObject expectedRefactoring = new MoveRenameMethodObject("A.java", "A",
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
        MoveRenameMethodObject firstRefactoring = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        // Rename method B.buzz -> C.foobar
        MoveRenameMethodObject secondRefactoring = new MoveRenameMethodObject("B.java", "B",
                buzz, "C.java", "C", foobar);
        // Rename Method A.foo -> B.bar
        MoveRenameMethodObject expectedRefactoring = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }

    private void doRenameMethodRenameMethodTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(null);
        boolean isTransitive = cell.checkTransitivity(firstRefactoring, secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
        }
        else {
            Assert.assertFalse(isTransitive);
        }
        MethodSignatureObject firstOriginalSignature = ((MoveRenameMethodObject) firstRefactoring).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((MoveRenameMethodObject) expectedRefactoring).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((MoveRenameMethodObject) firstRefactoring).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((MoveRenameMethodObject) expectedRefactoring).getDestinationMethodSignature();
        MethodSignatureObject secondOriginalSignature = ((MoveRenameMethodObject) secondRefactoring).getOriginalMethodSignature();
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
        Assert.assertEquals(((MoveRenameMethodObject) expectedRefactoring).getDestinationClassName(),
                ((MoveRenameMethodObject) firstRefactoring).getDestinationClassName());
        Assert.assertFalse(expectedOriginalSignature.equalsSignature(secondOriginalSignature));
        Assert.assertTrue(firstOriginalSignature.equalsSignature(expectedOriginalSignature));
        Assert.assertTrue(firstDestinationSignature.equalsSignature(expectedDestinationSignature));
    }
}
