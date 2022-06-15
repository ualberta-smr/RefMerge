package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RenameParameterPushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Push down method before rename parameter
     */
    public void testCheckCombination() {
        // Create foo(int value, int number)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "B";

        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList, "foo");

        // Create push down method refactoring A.foo(int value, int number) -> B.foo(int value, int number)
        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass,  originalClass, refactoredClass,  refactoredClass);
        pushDownMethodObject.setOriginalMethodSignature(foo);
        pushDownMethodObject.setDestinationMethodSignature(foo2);
        // Create parameter refactoring B.foo(int value, int number) -> B.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(refactoredClass, refactoredClass, foo3, foo4, parameterObject2, parameterObject3);
        RenameParameterPushDownMethodCell.checkCombination(pushDownMethodObject, renameParameterObject);

        // After combination:
        // push down method A.foo(int value, int number) -> B.foo(int value, int newNumber)
        // rename parameter A.foo(int value, int number) -> B.foo(int value, int newNumber)
        Assert.assertEquals(renameParameterObject.getOriginalClassName(), pushDownMethodObject.getOriginalClass());
        Assert.assertTrue(renameParameterObject.getOriginalMethodSignature().equalsSignature(pushDownMethodObject.getOriginalMethodSignature()));
        Assert.assertTrue(renameParameterObject.getRefactoredMethodSignature().equalsSignature(pushDownMethodObject.getDestinationMethodSignature()));
    }

    /*
     * Rename parameter before push down method
     */
    public void testCheckCombination2() {
        // Create foo(int value, int number)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "B";
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList, "foo");

        // Create push down method refactoring A.foo(int value, int newNumber) -> B.foo(int value, int newNumber)
        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass,  originalClass, refactoredClass,  refactoredClass);
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo4);
        // Create parameter refactoring A.foo(int value, int number) -> A.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(originalClass, originalClass, foo, foo2, parameterObject2, parameterObject3);
        RenameParameterPushDownMethodCell.checkCombination(pushDownMethodObject, renameParameterObject);

        // After combination:
        // push down method A.foo(int value, int number) -> B.foo(int value, int newNumber)
        // rename parameter A.foo(int value, int number) -> B.foo(int value, int newNumber)
        Assert.assertEquals(renameParameterObject.getOriginalClassName(), pushDownMethodObject.getOriginalClass());
        Assert.assertTrue(renameParameterObject.getOriginalMethodSignature().equalsSignature(pushDownMethodObject.getOriginalMethodSignature()));
        Assert.assertTrue(renameParameterObject.getRefactoredMethodSignature().equalsSignature(pushDownMethodObject.getDestinationMethodSignature()));

    }
}
