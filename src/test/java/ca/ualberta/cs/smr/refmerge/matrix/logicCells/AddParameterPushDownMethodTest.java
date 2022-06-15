package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.AddParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class AddParameterPushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Add parameter before push down method
     */
    public void testCheckCombination() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList2, "foo");
        String originalClass = "A";
        String newClass = "B";
        AddParameterObject addParameterObject = new AddParameterObject(originalClass, originalClass, foo1, foo2, parameterObject);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo4);
        AddParameterPushDownMethodCell.checkCombination(pushDownMethodObject, addParameterObject);
        Assert.assertTrue(pushDownMethodObject.getOriginalMethodSignature().equalsSignature(addParameterObject.getOriginalMethod()));
    }

    /*
     * Push down method before add parameter
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList2, "foo");
        String originalClass = "A";
        String newClass = "B";
        AddParameterObject addParameterObject = new AddParameterObject(newClass, newClass, foo3, foo4, parameterObject);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo1);
        pushDownMethodObject.setDestinationMethodSignature(foo2);
        AddParameterPushDownMethodCell.checkCombination(pushDownMethodObject, addParameterObject);
        Assert.assertTrue(pushDownMethodObject.getDestinationMethodSignature().equalsSignature(addParameterObject.getDestinationMethod()));
    }
}
