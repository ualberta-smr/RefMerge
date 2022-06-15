package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RemoveParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RemoveParameterPushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Remove parameter before push down method
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
        List<ParameterObject> parameterList3 = new ArrayList<>();
        parameterList3.add(parameterObject1);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList3, "foo");
        String originalClass = "A";
        String newClass = "B";
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(originalClass, originalClass, foo2, foo1, parameterObject);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo4);
        RemoveParameterPushDownMethodCell.checkCombination(pushDownMethodObject, removeParameterObject);
        Assert.assertTrue(pushDownMethodObject.getOriginalMethodSignature().equalsSignature(removeParameterObject.getOriginalMethod()));
    }

    /*
     * Push down method before remove parameter
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        List<ParameterObject> parameterList3 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        parameterList3.add(parameterObject1);
        parameterList3.add(parameterObject);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList3, "foo");
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList3, "foo");
        String originalClass = "A";
        String newClass = "B";
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(newClass, newClass, foo4, foo1, parameterObject);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo2);
        RemoveParameterPushDownMethodCell.checkCombination(pushDownMethodObject, removeParameterObject);
        Assert.assertTrue(pushDownMethodObject.getDestinationMethodSignature().equalsSignature(removeParameterObject.getDestinationMethod()));
    }
}
