package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
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

public class PushDownMethodExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    protected String getTestDataPath() {
        return "src/test/resources";
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
        MoveRenameMethodObject pushDownObject = null;
        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("addNumbers") && newName.equals("numbers")) {
                extractObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("doNumbers") && newName.equals("numbers")) {
                pushDownObject = new MoveRenameMethodObject(refactoring);
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

        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("ChildClass", "doNumbers", "ChildClass", "numbers");

        assert pushDownObject != null;
        pushDownMethodObject.setDestinationFilePath(pushDownObject.getDestinationFilePath());
        pushDownMethodObject.setOriginalMethodSignature(pushDownObject.getOriginalMethodSignature());
        pushDownMethodObject.setDestinationMethodSignature(pushDownObject.getDestinationMethodSignature());
        pushDownMethodObject.setOriginalFilePath(pushDownObject.getOriginalFilePath());

        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(project);
        boolean isConflict = cell.overrideConflict(extractMethodObject, pushDownMethodObject);
        Assert.assertTrue(isConflict);

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
        MoveRenameMethodObject pushDownObject = null;

        for(Refactoring refactoring : refactorings) {
            String originalName = ((RenameOperationRefactoring) refactoring).getOriginalOperation().getName();
            String newName = ((RenameOperationRefactoring) refactoring).getRenamedOperation().getName();
            if(originalName.equals("sumNumbers") && newName.equals("numbers")) {
                extractObject = new MoveRenameMethodObject(refactoring);
            }
            if(originalName.equals("multNumbers") && newName.equals("numbers")) {
                pushDownObject = new MoveRenameMethodObject(refactoring);
            }
        }

        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("Foo",
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

        assert pushDownObject != null;
        pushDownMethodObject.setDestinationFilePath(pushDownObject.getDestinationFilePath());
        pushDownMethodObject.setOriginalMethodSignature(pushDownObject.getOriginalMethodSignature());
        pushDownMethodObject.setDestinationMethodSignature(pushDownObject.getDestinationMethodSignature());
        pushDownMethodObject.setOriginalFilePath(pushDownObject.getOriginalFilePath());

        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(project);

        boolean isConflicting = cell.overloadConflict(extractMethodObject, pushDownMethodObject);
        Assert.assertTrue(isConflicting);
    }

    public void testNamingConflict() {
        MethodSignatureObject bar = new MethodSignatureObject(new ArrayList<>(), "bar");
        MethodSignatureObject foo = new MethodSignatureObject(new ArrayList<>(), "foo");
        // Ref 1: A.foo pushed down to B.foo
        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");
        // Ref 2: Extract B.foo from B.bar
        ExtractMethodObject extractMethodObject1 = new ExtractMethodObject("B.java", "B", bar,
                "B.java", "B", foo);
        // Ref 2: Extract B.bar from B.foo
        ExtractMethodObject extractMethodObject2 = new ExtractMethodObject("B.java", "B", foo,
                "B.java", "B", bar);
        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(getProject());

        boolean isConflicting = cell.conflictCell(extractMethodObject1, pushDownMethodObject);
        Assert.assertTrue(isConflicting);
        isConflicting = cell.conflictCell(extractMethodObject2, pushDownMethodObject);
        Assert.assertFalse(isConflicting);

    }

    public void testCheckCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(originalParameters, "bar");
        // Pull up method A.foo -> B.foo
        PushDownMethodObject pushDownMethodObject = new PushDownMethodObject("A", "foo", "B", "foo");
        // Extract method B.bar from B.foo
        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("B.java", "B", foo, "B.java", "B", bar);
        PushDownMethodExtractMethodCell cell = new PushDownMethodExtractMethodCell(null);
        cell.checkCombination(extractMethodObject, pushDownMethodObject);

        Assert.assertEquals(pushDownMethodObject.getOriginalClass(), extractMethodObject.getOriginalClassName());
        Assert.assertEquals(pushDownMethodObject.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());

    }

}
