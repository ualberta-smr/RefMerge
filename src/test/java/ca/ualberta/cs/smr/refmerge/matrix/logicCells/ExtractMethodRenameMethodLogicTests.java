package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class ExtractMethodRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckExtractMethodRenameMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";

        String configurePath = "extractMethodRenameMethodFiles/refactored/OverloadInheritance.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(0));
        RefactoringObject renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(0));
        ExtractMethodMoveRenameMethodCell cell = new ExtractMethodMoveRenameMethodCell(project);
        boolean isDependent = cell.checkOverrideConflict(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isDependent);
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(2));
        isDependent = cell.checkOverrideConflict(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isDependent);
        extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(3));
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(5));
        isDependent = cell.checkOverrideConflict(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isDependent);
        extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(1));
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(3));

        configurePath = "extractMethodRenameMethodFiles/refactored/Override.java";
        myFixture.configureByFiles(configurePath);

        isDependent = cell.checkOverrideConflict(renameMethodObject, extractMethodObject);
        Assert.assertTrue(isDependent);
    }

    public void testCheckExtractMethodRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";

        String configurePath = "extractMethodRenameMethodFiles/refactored/OverloadInheritance.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(0));
        RefactoringObject renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(0));
        ExtractMethodMoveRenameMethodCell cell = new ExtractMethodMoveRenameMethodCell(project);
        boolean isConflicting = cell.checkOverloadConflict(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isConflicting);
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(2));
        isConflicting = cell.checkOverloadConflict(renameMethodObject, extractMethodObject);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckExtractMethodRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(2));
        RefactoringObject renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(4));
        ExtractMethodMoveRenameMethodCell cell = new ExtractMethodMoveRenameMethodCell(project);
        boolean isDependent = cell.checkMethodNamingConflict(renameMethodObject, extractMethodObject);
        Assert.assertTrue(isDependent);
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(3));
        isDependent = cell.checkMethodNamingConflict(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isDependent);
    }

    public void testCheckExtractMethodRenameMethodDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameMethodFiles/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalPath, refactoredPath);
        List<Refactoring> renameMethodRefactorings = GetDataForTests.getRefactorings("RENAME_METHOD",
                originalPath, refactoredPath);

        assert extractMethodRefactorings != null;
        assert renameMethodRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(0));
        RefactoringObject renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(1));
        boolean isDependent = ExtractMethodMoveRenameMethodCell.checkDependence(renameMethodObject,
                extractMethodObject);
        Assert.assertTrue(isDependent);
        renameMethodObject = RefactoringObjectUtils.createRefactoringObject(renameMethodRefactorings.get(2));
        isDependent = ExtractMethodMoveRenameMethodCell.checkDependence(renameMethodObject, extractMethodObject);
        Assert.assertFalse(isDependent);
    }

    public void testCheckExtractMethodRenameMethodTransitivity() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject efoo = new MethodSignatureObject(originalParameters, "efoo");
        MethodSignatureObject ebar = new MethodSignatureObject(originalParameters, "ebar");
        // Extract method A.efoo from A.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("A.java", "A", foo,
                "A.java", "A", efoo);
        // Rename method A.efoo -> A.ebar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("A.java", "A",
                efoo, "A.java", "A", ebar);
        // Extract method A.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", foo,
                "A.java", "A", ebar);
        doExtractMethodRenameMethodTest(moveRenameMethodObject, extractMethodObject, expectedRefactoring, true);
    }

    public void testCheckExtractMethodRenameMethodCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        MethodSignatureObject ebar = new MethodSignatureObject(originalParameters, "ebar");
        // Extract method A.ebar from A.bar
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("A.java", "A", bar,
                "A.java", "A", ebar);
        // Rename method A.foo -> A.bar
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("A.java", "A", foo,
                "A.java", "A", bar);
        // Extract method A.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", foo,
                "A.java", "A", ebar);
        doExtractMethodRenameMethodTest(moveRenameMethodObject, extractMethodObject, expectedRefactoring, false);
    }


    private void doExtractMethodRenameMethodTest(RefactoringObject renameMethodObject, RefactoringObject extractMethodObject,
                                               RefactoringObject expectedRefactoring, boolean expectedTransitivity) {

        boolean isTransitive = ExtractMethodMoveRenameMethodCell.checkTransitivity(renameMethodObject,
                extractMethodObject);

        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
        }
        else {
            Assert.assertFalse(isTransitive);
        }

        MethodSignatureObject firstOriginalSignature = ((ExtractMethodObject) extractMethodObject).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((ExtractMethodObject) expectedRefactoring).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((ExtractMethodObject) extractMethodObject).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((ExtractMethodObject) expectedRefactoring).getDestinationMethodSignature();

        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), extractMethodObject.getDestinationFilePath());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getOriginalClassName(),
                ((ExtractMethodObject) extractMethodObject).getOriginalClassName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getDestinationClassName(),
                ((ExtractMethodObject) extractMethodObject).getDestinationClassName());
        Assert.assertTrue(expectedOriginalSignature.equalsSignature(firstOriginalSignature));
        Assert.assertTrue(expectedDestinationSignature.equalsSignature(firstDestinationSignature));
    }

}
