package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ChangeParameterTypeObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ChangeParameterTypePullUpMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Change parameter type before pull up method
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

        PullUpMethodObject pullUpMethodObject =
                new PullUpMethodObject(originalClass, "foo", newClass, "foo");
        pullUpMethodObject.setOriginalMethodSignature(foo3);
        pullUpMethodObject.setDestinationMethodSignature(foo4);
        ChangeParameterTypePullUpMethodCell.checkCombination(pullUpMethodObject, changeParameterTypeObject);
        Assert.assertTrue(pullUpMethodObject.getOriginalMethodSignature().equalsSignature(changeParameterTypeObject.getOriginalMethod()));
    }

    /*
     * Pull up method before change parameter type
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

        PullUpMethodObject pullUpMethodObject =
                new PullUpMethodObject(originalClass, "foo", newClass, "foo");
        pullUpMethodObject.setOriginalMethodSignature(foo1);
        pullUpMethodObject.setDestinationMethodSignature(foo2);
        ChangeParameterTypePullUpMethodCell.checkCombination(pullUpMethodObject, changeParameterTypeObject);
        Assert.assertTrue(pullUpMethodObject.getDestinationMethodSignature().equalsSignature(changeParameterTypeObject.getDestinationMethod()));
    }
}
