package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class MoveRenameClassMoveRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckRenameMethodRenameClassDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring methodRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring classRef = classRefs.get(0);
        RefactoringObject methodRefactoringObject = RefactoringObjectUtils.createRefactoringObject(methodRef);
        RefactoringObject classRefactoringObject = RefactoringObjectUtils.createRefactoringObject(classRef);
        boolean isDependent = MoveRenameClassMoveRenameMethodCell.checkDependence(methodRefactoringObject,
                classRefactoringObject);
        Assert.assertTrue(isDependent);
    }

    public void testFoundRenameClassRenameMethodCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Rename class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Rename method A.foo -> B.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        // Rename Method A.foo -> B.bar
        MoveRenameMethodObject expectedMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);

        doRenameClassRenameMethodTest(moveRenameMethodObject, moveRenameClassObject, expectedMethodObject);
    }

    public void testFoundRenameClassRenameMethodCombination2() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Rename class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Rename method A.foo -> A.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "A.java", "A", bar);
        // Rename Method A.foo -> B.bar
        MoveRenameMethodObject expectedMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        doRenameClassRenameMethodTest(moveRenameMethodObject, moveRenameClassObject, expectedMethodObject);
    }

    public void testFoundRenameClassRenameMethodCombination3() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Rename class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Rename method B.foo -> B.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("B.java", "B",
                foo, "B.java", "B", bar);
        // Rename Method A.foo -> B.bar
        MoveRenameMethodObject expectedMethodObject = new MoveRenameMethodObject("A.java", "A",
                foo, "B.java", "B", bar);
        doRenameClassRenameMethodTest(moveRenameMethodObject, moveRenameClassObject, expectedMethodObject);
    }


    public void testNotFoundRenameClassRenameMethodCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Rename class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "b.java", "B", "package");
        // Rename method C.foo -> C.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("C.java", "C", foo,
                "C.java", "C", bar);
        // Rename Method C.foo -> C.bar
        MoveRenameMethodObject expectedMethodObject = new MoveRenameMethodObject("C.java", "C", foo,
                "C.java", "C", bar);
        doRenameClassRenameMethodTest(moveRenameMethodObject, moveRenameClassObject, expectedMethodObject);
    }

    private void doRenameClassRenameMethodTest(RefactoringObject renameMethodObject, RefactoringObject renameClassObject,
                                               RefactoringObject expectedRefactoring) {
        MoveRenameClassMoveRenameMethodCell.checkCombination(renameMethodObject, renameClassObject);

        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), renameMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), renameMethodObject.getDestinationFilePath());
        Assert.assertEquals(((MoveRenameMethodObject) expectedRefactoring).getDestinationClassName(),
                ((MoveRenameMethodObject) renameMethodObject).getDestinationClassName());
        Assert.assertEquals(((MoveRenameMethodObject) expectedRefactoring).getOriginalClassName(),
                ((MoveRenameMethodObject) renameMethodObject).getOriginalClassName());
    }

}
