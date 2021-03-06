package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.AddParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class AddParameterExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Add parameter to source method
     */
    public void testCheckCombination() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList, "bar");
        String originalClass = "A";
        AddParameterObject addParameterObject = new AddParameterObject(originalClass, originalClass, foo1, foo2, parameterObject);

        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("A.java", originalClass, foo3, "A.java", originalClass, bar4);
        AddParameterExtractMethodCell.checkCombination(extractMethodObject, addParameterObject);
        Assert.assertTrue(extractMethodObject.getOriginalMethodSignature().equalsSignature(addParameterObject.getOriginalMethod()));
    }

    /*
     * Add parameter to extracted method
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList, "bar");
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList, "bar");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList2, "bar");
        String originalClass = "A";
        AddParameterObject addParameterObject = new AddParameterObject(originalClass, originalClass, bar3, bar4, parameterObject);

        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("A.java", originalClass, foo1, "A.java", originalClass, bar2);
        AddParameterExtractMethodCell.checkCombination(extractMethodObject, addParameterObject);
        Assert.assertTrue(extractMethodObject.getDestinationMethodSignature().equalsSignature(addParameterObject.getDestinationMethod()));
    }
}
