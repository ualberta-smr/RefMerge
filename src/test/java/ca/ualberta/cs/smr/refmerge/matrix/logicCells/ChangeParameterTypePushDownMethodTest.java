package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ChangeParameterTypeObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ChangeParameterTypePushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Change parameter type before push down method
     */
    public void testCheckCombination() {
        ParameterObject parameterObject = new ParameterObject("double", "param");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        ParameterObject parameterObject2 = new ParameterObject("float", "param");
        List<ParameterObject> parameterList = new ArrayList<>();
        List<ParameterObject> parameterList2 = new ArrayList<>();
        List<ParameterObject> parameterList3 = new ArrayList<>();
        List<ParameterObject> parameterList4 = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        parameterList2.add(parameterObject);
        parameterList2.add(parameterObject2);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        parameterList3.add(parameterObject);
        parameterList3.add(parameterObject1);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList3, "foo");
        parameterList4.add(parameterObject);
        parameterList4.add(parameterObject1);
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList4, "foo");
        String originalClass = "A";
        String newClass = "B";
        ChangeParameterTypeObject changeParameterTypeObject =
                new ChangeParameterTypeObject(originalClass, originalClass, foo1, foo2, parameterObject, parameterObject2);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo3);
        pushDownMethodObject.setDestinationMethodSignature(foo4);
        ChangeParameterTypePushDownMethodCell.checkCombination(pushDownMethodObject, changeParameterTypeObject);
        Assert.assertTrue(pushDownMethodObject.getOriginalMethodSignature().equalsSignature(changeParameterTypeObject.getOriginalMethod()));
    }

    /*
     * Push down method before change parameter type
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "param");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        ParameterObject parameterObject2 = new ParameterObject("float", "param");
        List<ParameterObject> parameterList = new ArrayList<>();
        List<ParameterObject> parameterList2 = new ArrayList<>();
        List<ParameterObject> parameterList3 = new ArrayList<>();
        List<ParameterObject> parameterList4 = new ArrayList<>();
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        parameterList2.add(parameterObject);
        parameterList2.add(parameterObject1);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        parameterList3.add(parameterObject);
        parameterList3.add(parameterObject1);
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList3, "foo");
        parameterList4.add(parameterObject2);
        parameterList4.add(parameterObject1);
        MethodSignatureObject foo4 = new MethodSignatureObject(parameterList4, "foo");
        String originalClass = "A";
        String newClass = "B";
        ChangeParameterTypeObject changeParameterTypeObject =
                new ChangeParameterTypeObject(newClass, newClass, foo3, foo4, parameterObject, parameterObject2);

        PushDownMethodObject pushDownMethodObject =
                new PushDownMethodObject(originalClass, "foo", newClass, "foo");
        pushDownMethodObject.setOriginalMethodSignature(foo1);
        pushDownMethodObject.setDestinationMethodSignature(foo2);
        ChangeParameterTypePushDownMethodCell.checkCombination(pushDownMethodObject, changeParameterTypeObject);
        Assert.assertTrue(pushDownMethodObject.getDestinationMethodSignature().equalsSignature(changeParameterTypeObject.getDestinationMethod()));
    }
}
