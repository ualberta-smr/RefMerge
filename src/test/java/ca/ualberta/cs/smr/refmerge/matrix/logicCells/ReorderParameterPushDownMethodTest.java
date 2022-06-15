package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.ReorderParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ReorderParameterPushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Reorder parameters before push down method
     */
    public void testCheckCombination() {
        ParameterObject parameterObject = new ParameterObject("double", "param");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        ParameterObject parameterObject2 = new ParameterObject("float", "y");
        List<ParameterObject> parameterList = new ArrayList<>();
        List<ParameterObject> parameterList2 = new ArrayList<>();
        List<ParameterObject> parameterList3 = new ArrayList<>();
        List<ParameterObject> parameterList4 = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject2);
        parameterList2.add(parameterObject);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        parameterList3.add(parameterObject1);
        parameterList3.add(parameterObject2);
        parameterList3.add(parameterObject);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList3, "foo");
        parameterList4.add(parameterObject1);
        parameterList4.add(parameterObject2);
        parameterList4.add(parameterObject);
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList4, "foo");
        String originalClass = "A";
        String newClass = "B";
        ReorderParameterObject reorderParameterObject = new ReorderParameterObject(originalClass, originalClass, foo1, foo2);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo4);
        ReorderParameterPushDownMethodCell.checkCombination(pushDownMethodObject, reorderParameterObject);
        Assert.assertTrue(pushDownMethodObject.getOriginalMethodSignature().equalsSignature(reorderParameterObject.getOriginalMethod()));
    }

    /*
     * Push down method before add parameter
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "param");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        ParameterObject parameterObject2 = new ParameterObject("float", "y");
        List<ParameterObject> parameterList = new ArrayList<>();
        List<ParameterObject> parameterList2 = new ArrayList<>();
        List<ParameterObject> parameterList3 = new ArrayList<>();
        List<ParameterObject> parameterList4 = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject2);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList3, "foo");
        parameterList4.add(parameterObject1);
        parameterList4.add(parameterObject2);
        parameterList4.add(parameterObject);
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList4, "foo");
        String originalClass = "A";
        String newClass = "B";
        ReorderParameterObject reorderParameterObject = new ReorderParameterObject(newClass, newClass, foo3, foo4);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo1);
        pushDownMethodObject.setDestinationMethodSignature(foo2);
        ReorderParameterPushDownMethodCell.checkCombination(pushDownMethodObject, reorderParameterObject);
        Assert.assertTrue(pushDownMethodObject.getDestinationMethodSignature().equalsSignature(reorderParameterObject.getDestinationMethod()));
    }
}
