package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RenameParameterRenameParameterTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "number1");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject4 = new ParameterObject("int", "number2");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject4);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "A";
        // Rename parameter A.foo.number -> A.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject2, parameterObject3);
        // Rename parameter A.foo.number -> A.foo.number2
        RenameParameterObject renameParameterObject2 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo3, parameterObject2, parameterObject4);

        boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(renameParameterObject1, renameParameterObject2);
        Assert.assertTrue(isConflicting);
    }

    public void testNamingConflict2() {
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "number1");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject3);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "A";
        // Rename parameter A.foo.value -> A.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject, parameterObject3);
        // Rename parameter A.foo.number -> A.foo.number1
        RenameParameterObject renameParameterObject2 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo3, parameterObject2, parameterObject3);

        boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(renameParameterObject1, renameParameterObject2);
        Assert.assertTrue(isConflicting);
    }

    public void testNamingConflict3() {
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "number1");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "A";
        // Rename parameter A.foo.value -> A.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject, parameterObject3);
        // Rename parameter A.foo.value -> A.foo.number1
        RenameParameterObject renameParameterObject2 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject, parameterObject3);

        boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(renameParameterObject1, renameParameterObject2);
        Assert.assertFalse(isConflicting);
    }

    public void testNamingConflict4() {
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        ParameterObject parameterObject3 = new ParameterObject("double", "val");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        parameterObject = new ParameterObject("int", "number1");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject4 = new ParameterObject("int", "number2");
        parameterList = new ArrayList<>();
        parameterObject = new ParameterObject("int", "value");
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "A";
        // Rename parameter A.foo.value -> A.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject, parameterObject3);
        // Rename parameter A.foo.value -> A.foo.number2
        RenameParameterObject renameParameterObject2 =
                new RenameParameterObject(originalClass, refactoredClass, foo3, foo3, parameterObject, parameterObject4);

        // Should be false because of different method signatures
        boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(renameParameterObject1, renameParameterObject2);
        Assert.assertFalse(isConflicting);
    }

    public void testTransitivity() {
        ParameterObject parameterObject = new ParameterObject("int", "value");
        ParameterObject parameterObject2 = new ParameterObject("int", "number");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject3 = new ParameterObject("int", "number1");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject3);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        ParameterObject parameterObject4 = new ParameterObject("int", "number2");
        parameterList = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject4);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        String originalClass = "A";
        String refactoredClass = "A";
        // Rename parameter A.foo.number -> A.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject(originalClass, refactoredClass, foo, foo2, parameterObject2, parameterObject3);
        // Rename parameter A.foo.number1 -> A.foo.number2
        RenameParameterObject renameParameterObject2 =
                new RenameParameterObject(originalClass, refactoredClass, foo2, foo3, parameterObject2, parameterObject4);

        boolean isTransitive = RenameParameterRenameParameterCell.checkTransitivity(renameParameterObject1, renameParameterObject2);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(renameParameterObject1.getRefactoredParameterObject().getName(), renameParameterObject2.getRefactoredParameterObject().getName());
    }


}
