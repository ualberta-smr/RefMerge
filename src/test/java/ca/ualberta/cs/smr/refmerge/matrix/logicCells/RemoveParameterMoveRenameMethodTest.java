package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RemoveParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RemoveParameterMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Remove parameter before move + rename method
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
        MethodSignatureObject foo3 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList, "bar");
        String originalClass = "A";
        String newClass = "B";
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(originalClass, originalClass, foo2, foo1, parameterObject);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo3, "B.java", newClass, bar4);
        RemoveParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, removeParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getOriginalMethodSignature().equalsSignature(removeParameterObject.getOriginalMethod()));
    }

    /*
     * Move + rename method before remove parameter
     */
    public void testCheckCombination2() {
        ParameterObject parameterObject = new ParameterObject("double", "newParam");
        ParameterObject parameterObject1 = new ParameterObject("int", "x");
        List<ParameterObject> parameterList = new ArrayList<>();
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject);
        List<ParameterObject> parameterList2 = parameterList;
        MethodSignatureObject foo1 = new MethodSignatureObject(parameterList, "foo");
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList2, "bar");
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList, "bar");
        List<ParameterObject> parameterList3 = new ArrayList<>();
        parameterList3.add(parameterObject1);
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList3, "bar");
        String originalClass = "A";
        String newClass = "B";
        RemoveParameterObject removeParameterObject = new RemoveParameterObject(newClass, newClass, bar4, bar3, parameterObject);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo1, "B.java", newClass, bar2);
        RemoveParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, removeParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getDestinationMethodSignature().equalsSignature(removeParameterObject.getDestinationMethod()));
    }
}
