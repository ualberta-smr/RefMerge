package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RemoveParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RemoveParameterExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Remove parameter from source method
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
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject foo2 = new MethodSignatureObject(parameterList2, "foo");
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList2, "bar");
        String originalClass = "A";
        // Reverse order of methods for remove parameter (compared to add parameter)
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(originalClass, originalClass, foo2, foo1, parameterObject);

        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("A.java", originalClass, foo3, "A.java", originalClass, bar4);
        RemoveParameterExtractMethodCell.checkCombination(extractMethodObject, removeParameterObject);
        Assert.assertTrue(extractMethodObject.getOriginalMethodSignature().equalsSignature(removeParameterObject.getOriginalMethod()));
    }

    /*
     * Remove parameter from extracted method
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList, "bar");
        List<ParameterObject> parameterList2 = new ArrayList<>();
        parameterList2.add(parameterObject1);
        parameterList2.add(parameterObject);
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList2, "bar");
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList2, "bar");
        String originalClass = "A";
        // Reverse order for remove parameter
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(originalClass, originalClass, bar4, bar3, parameterObject);

        ExtractMethodObject extractMethodObject =
                new ExtractMethodObject("A.java", originalClass, foo1, "A.java", originalClass, bar2);
        RemoveParameterExtractMethodCell.checkCombination(extractMethodObject, removeParameterObject);
        Assert.assertTrue(extractMethodObject.getDestinationMethodSignature().equalsSignature(removeParameterObject.getDestinationMethod()));
    }
}
