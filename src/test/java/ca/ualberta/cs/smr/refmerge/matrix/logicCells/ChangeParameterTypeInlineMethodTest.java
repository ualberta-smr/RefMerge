package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ChangeParameterTypeObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ChangeParameterTypeInlineMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Change parameter type performed on source method
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
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList4, "bar");
        String originalClass = "A";
        ChangeParameterTypeObject changeParameterTypeObject =
                new ChangeParameterTypeObject(originalClass, originalClass, foo1, foo2, parameterObject, parameterObject2);

        InlineMethodObject inlineMethodObject =
                new InlineMethodObject("A.java", originalClass, foo3, "A.java", originalClass, bar4);
        ChangeParameterTypeInlineMethodCell.checkCombination(inlineMethodObject, changeParameterTypeObject);
        Assert.assertTrue(inlineMethodObject.getOriginalMethodSignature().equalsSignature(changeParameterTypeObject.getDestinationMethod()));
    }

    /*
     * Change parameter type performed on target method
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
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList2, "bar");
        parameterList3.add(parameterObject);
        parameterList3.add(parameterObject1);
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList3, "bar");
        parameterList4.add(parameterObject2);
        parameterList4.add(parameterObject1);
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList4, "bar");
        String originalClass = "A";
        ChangeParameterTypeObject changeParameterTypeObject =
                new ChangeParameterTypeObject(originalClass, originalClass, bar3, bar4, parameterObject, parameterObject2);

        InlineMethodObject inlineMethodObject =
                new InlineMethodObject("A.java", originalClass, foo1, "A.java", originalClass, bar2);
        ChangeParameterTypeInlineMethodCell.checkCombination(inlineMethodObject, changeParameterTypeObject);
        Assert.assertTrue(inlineMethodObject.getDestinationMethodSignature().equalsSignature(changeParameterTypeObject.getDestinationMethod()));
    }
}
