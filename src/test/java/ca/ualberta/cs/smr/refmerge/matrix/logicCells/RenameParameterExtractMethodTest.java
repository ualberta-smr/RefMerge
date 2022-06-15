package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RenameParameterExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Create foo(int value, int number)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "B";
        // Create bar(int value, int number)
        MethodSignatureObject bar = new MethodSignatureObject(parameterList, "bar");
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList, "bar");
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList, "bar");

        // Create extract method refactoring A.foo(int value, int number) extracted to B.bar(int value, int number)
        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject(originalClass, originalClass, foo, refactoredClass, refactoredClass, bar);
        // Create parameter refactoring B.bar(int value, int number) -> B.bar(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(refactoredClass, refactoredClass, bar2, bar3, parameterObject2, parameterObject3);
        RenameParameterExtractMethodCell.checkCombination(extractMethodObject, renameParameterObject);

        Assert.assertTrue(extractMethodObject.getDestinationMethodSignature().equalsSignature(renameParameterObject.getRefactoredMethodSignature()));

    }

    public void testCheckCombination2() {
        // Create foo(int value, int number)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "B";
        // Create bar(int value, int number)
        MethodSignatureObject bar = new MethodSignatureObject(parameterList, "bar");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");

        // Create extract method refactoring A.foo(int value, int number) extracted to B.bar(int value, int number)
        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject(originalClass, originalClass, foo, refactoredClass, refactoredClass, bar);
        // Create parameter refactoring A.foo(int value, int number) -> A.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(originalClass, originalClass, foo2, foo3, parameterObject2, parameterObject3);
        RenameParameterExtractMethodCell.checkCombination(extractMethodObject, renameParameterObject);

        Assert.assertTrue(extractMethodObject.getOriginalMethodSignature().equalsSignature(renameParameterObject.getRefactoredMethodSignature()));

    }

    public void testCheckCombination3() {
        // Create foo(int value, int number)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "newNumber");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "B";
        // Create bar(int value, int number)
        MethodSignatureObject bar = new MethodSignatureObject(parameterList, "bar");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");

        // Create extract method refactoring A.foo(int value, int newNumber) extracted to B.bar(int value, int number)
        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject(originalClass, originalClass, foo, refactoredClass, refactoredClass, bar);
        // Create parameter refactoring A.foo(int value, int number) -> A.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(originalClass, originalClass, foo2, foo3, parameterObject2, parameterObject3);
        RenameParameterExtractMethodCell.checkCombination(extractMethodObject, renameParameterObject);

        Assert.assertTrue(extractMethodObject.getOriginalMethodSignature().equalsSignature(renameParameterObject.getRefactoredMethodSignature()));

    }

}
