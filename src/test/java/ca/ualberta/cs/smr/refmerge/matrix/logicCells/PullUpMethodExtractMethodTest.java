package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class PullUpMethodExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pulled up to B.foo
        PullUpMethodObject pullUpMethodObject1 = new PullUpMethodObject("A", "foo", "B", "foo");
        // Ref 2: Extract B.foo from B.bar
        ExtractMethodObject extractMethodObject1 = new ExtractMethodObject("B.java", "B", bar,
                "B.java", "B", foo);
        // Ref 2: Extract B.bar from B.foo
        ExtractMethodObject extractMethodObject2 = new ExtractMethodObject("B.java", "B", foo,
                "B.java", "B", bar);
        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(extractMethodObject1, pullUpMethodObject1);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(extractMethodObject2, pullUpMethodObject1);
        Assert.assertFalse(isConflicting);

    }

    public void testOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        //Reuse the existing overload files to get PSI structure
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverloadConflict/original/OverloadClasses.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 3;

        MoveRenameMethodObject extractObject = null;
        MoveRenameMethodObject pullUpObject = null;

        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("sumNumbers") && newName.equals("numbers")) {
                extractObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("multNumbers") && newName.equals("numbers")) {
                pullUpObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("Foo",
                "multNumbers", "Foo", "numbers");
        assert extractObject != null;
        String originalFilePath = extractObject.getOriginalFilePath();
        String newFilePath = extractObject.getDestinationFilePath();
        String originalClassName = extractObject.getOriginalClassName();
        String newClassName = extractObject.getDestinationClassName();
        MethodSignatureObject originalSignature = extractObject.getOriginalMethodSignature();
        MethodSignatureObject extractedSignature = extractObject.getDestinationMethodSignature();
        ExtractMethodObject extractMethodObject = new ExtractMethodObject(originalFilePath, originalClassName,
                originalSignature, newFilePath, newClassName, extractedSignature);

        assert pullUpObject != null;
        pullUpMethodObject.setDestinationFilePath(pullUpObject.getDestinationFilePath());
        pullUpMethodObject.setOriginalMethodSignature(pullUpObject.getOriginalMethodSignature());
        pullUpMethodObject.setDestinationMethodSignature(pullUpObject.getDestinationMethodSignature());
        pullUpMethodObject.setOriginalFilePath(pullUpObject.getOriginalFilePath());

        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(project);

        boolean isConflicting = cell.overloadConflict(extractMethodObject, pullUpMethodObject);
        Assert.assertTrue(isConflicting);
    }
    public void testOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        // Reuse rename method files to get PSI structure for override test
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        MoveRenameMethodObject extractObject = null;
        MoveRenameMethodObject pullUpObject = null;
        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("addNumbers") && newName.equals("numbers")) {
                extractObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("doNumbers") && newName.equals("numbers")) {
                pullUpObject = new MoveRenameMethodObject(refactoring);
            }
        }

        assert extractObject != null;
        String originalFilePath = extractObject.getOriginalFilePath();
        String newFilePath = extractObject.getDestinationFilePath();
        String originalClassName = extractObject.getOriginalClassName();
        String newClassName = extractObject.getDestinationClassName();
        MethodSignatureObject originalSignature = extractObject.getOriginalMethodSignature();
        MethodSignatureObject extractedSignature = extractObject.getDestinationMethodSignature();
        ExtractMethodObject extractMethodObject = new ExtractMethodObject(originalFilePath, originalClassName,
                originalSignature, newFilePath, newClassName, extractedSignature);

        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("ChildClass", "doNumbers", "ChildClass", "numbers");

        assert pullUpObject != null;
        pullUpMethodObject.setDestinationFilePath(pullUpObject.getDestinationFilePath());
        pullUpMethodObject.setOriginalMethodSignature(pullUpObject.getOriginalMethodSignature());
        pullUpMethodObject.setDestinationMethodSignature(pullUpObject.getDestinationMethodSignature());
        pullUpMethodObject.setOriginalFilePath(pullUpObject.getOriginalFilePath());

        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(project);
        boolean isConflict = cell.overrideConflict(extractMethodObject, pullUpMethodObject);
        Assert.assertTrue(isConflict);

    }

    public void testCheckCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Pull up method A.foo -> B.foo
        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("A", "foo", "B", "foo");
        // Extract method B.bar from B.foo
        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("B.java", "B", foo, "B.java", "B", bar);
        PullUpMethodExtractMethodCell cell = new PullUpMethodExtractMethodCell(null);
        cell.checkCombination(extractMethodObject, pullUpMethodObject);

        Assert.assertEquals(pullUpMethodObject.getOriginalClass(), extractMethodObject.getOriginalClassName());
        Assert.assertEquals(pullUpMethodObject.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());

    }

}
