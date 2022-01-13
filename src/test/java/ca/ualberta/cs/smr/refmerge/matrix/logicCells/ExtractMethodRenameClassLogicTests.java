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
import java.util.Objects;

public class ExtractMethodRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckExtractMethodMoveRenameClassDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION", originalPath, refactoredPath);
        assert extractMethodRefactorings != null;
        extractMethodRefactorings.addAll(Objects.requireNonNull(GetDataForTests.getRefactorings("EXTRACT_AND_MOVE_OPERATION",
                originalPath, refactoredPath)));
        List<Refactoring> renameClassRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert renameClassRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(0));
        RefactoringObject renameClassObject = RefactoringObjectUtils.createRefactoringObject(renameClassRefactorings.get(0));
        boolean isDependent = ExtractMethodMoveRenameClassCell.checkDependence(renameClassObject, extractMethodObject);
        Assert.assertFalse(isDependent);
        extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(1));
        isDependent = ExtractMethodMoveRenameClassCell.checkDependence(renameClassObject, extractMethodObject);
        Assert.assertTrue(isDependent);
    }

    public void testCheckExtractMethodMoveRenameClassCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject efoo = new MethodSignatureObject(originalParameters, "efoo");
        MethodSignatureObject ebar = new MethodSignatureObject(originalParameters, "ebar");
        // Extract method A.efoo from A.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("A.java", "A", foo,
                "A.java", "A", efoo);
        // Rename Class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", foo,
                "B.java", "B", ebar);
        doExtractMethodMoveRenameClassTest(moveRenameClassObject, extractMethodObject, expectedRefactoring);
    }

    public void testCheckExtractMethodMoveRenameClassCombination2() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject efoo = new MethodSignatureObject(originalParameters, "efoo");
        MethodSignatureObject ebar = new MethodSignatureObject(originalParameters, "ebar");
        // Extract method B.efoo from B.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("B.java", "B", foo,
                "B.java", "B", efoo);
        // Rename Class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", foo,
                "B.java", "B", ebar);
        doExtractMethodMoveRenameClassTest(moveRenameClassObject, extractMethodObject, expectedRefactoring);
    }

    public void testCheckExtractMethodMoveRenameClassCombinationInSameFile() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject efoo = new MethodSignatureObject(originalParameters, "efoo");
        MethodSignatureObject ebar = new MethodSignatureObject(originalParameters, "ebar");
        // Extract method B.efoo from B.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("Foo.java", "B", foo,
                "Foo.java", "B", efoo);
        // Rename Class A -> B
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("Foo.java", "A", "package",
                "Foo.java", "B", "package");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("Foo.java", "A", foo,
                "Foo.java", "B", ebar);
        doExtractMethodMoveRenameClassTest(moveRenameClassObject, extractMethodObject, expectedRefactoring);
    }


    private void doExtractMethodMoveRenameClassTest(RefactoringObject renameClassObject, RefactoringObject extractMethodObject,
                                                RefactoringObject expectedRefactoring) {

        ExtractMethodMoveRenameClassCell.checkCombination(renameClassObject, extractMethodObject);

        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), extractMethodObject.getDestinationFilePath());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getOriginalClassName(),
                ((ExtractMethodObject) extractMethodObject).getOriginalClassName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getDestinationClassName(),
                ((ExtractMethodObject) extractMethodObject).getDestinationClassName());

    }
}
