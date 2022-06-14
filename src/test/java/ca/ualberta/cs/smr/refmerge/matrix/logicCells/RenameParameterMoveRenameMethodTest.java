package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RenameParameterMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

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
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        // Create foo(int value, int newNumber)
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");

        // Create method refactoring A.foo(int value, int number) -> B.bar(int value, int number)
        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject(originalClass, originalClass, foo, refactoredClass, refactoredClass, bar);
        // Create parameter refactoring B.foo(int value, int number) -> B.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(refactoredClass, refactoredClass, foo, foo2, parameterObject2, parameterObject3);
        RenameParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, renameParameterObject);

        Assert.assertEquals(renameParameterObject.getRefactoredClassName(), moveRenameMethodObject.getDestinationClassName());
        Assert.assertTrue(moveRenameMethodObject.getOriginalMethodSignature().equalsSignature(renameParameterObject.getOriginalMethodSignature()));
        Assert.assertTrue(moveRenameMethodObject.getDestinationMethodSignature().equalsSignature(renameParameterObject.getRefactoredMethodSignature()));

    }

    public void testCheckCombination2() {
        // Create foo(int value, int newNumber)
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        ParameterObject parameterObject3 = new ParameterObject("int", "newNumber");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        String originalClass = "A";
        String refactoredClass = "B";
        // Create bar(int value, int newNumber)
        MethodSignatureObject bar = new MethodSignatureObject(parameterList, "bar");
        // Create foo(int value, int number)
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");

        // Create method refactoring A.foo(int value, int newNumber) -> B.bar(int value, int newNumber)
        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject(originalClass, originalClass, foo2, refactoredClass, refactoredClass, bar);
        // Create parameter refactoring A.foo(int value, int number) -> A.foo(int value, int newNumber)
        RenameParameterObject renameParameterObject =
                new RenameParameterObject(originalClass, originalClass, foo, foo3, parameterObject2, parameterObject3);
        RenameParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, renameParameterObject);

        Assert.assertEquals(renameParameterObject.getRefactoredClassName(), moveRenameMethodObject.getDestinationClassName());
        Assert.assertTrue(moveRenameMethodObject.getOriginalMethodSignature().equalsSignature(renameParameterObject.getOriginalMethodSignature()));
        Assert.assertTrue(moveRenameMethodObject.getDestinationMethodSignature().equalsSignature(renameParameterObject.getRefactoredMethodSignature()));

    }

}
