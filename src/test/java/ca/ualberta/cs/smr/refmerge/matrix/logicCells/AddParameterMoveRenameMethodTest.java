package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.AddParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class AddParameterMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Add parameter before move + rename method
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
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList2, "bar");
        String originalClass = "A";
        String newClass = "B";
        AddParameterObject addParameterObject = new AddParameterObject(originalClass, originalClass, foo1, foo2, parameterObject);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo3, "B.java", newClass, bar4);
        AddParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, addParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getOriginalMethodSignature().equalsSignature(addParameterObject.getOriginalMethod()));
    }

    /*
     * Move + rename method before add parameter
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
        String newClass = "B";
        AddParameterObject addParameterObject = new AddParameterObject(newClass, newClass, bar3, bar4, parameterObject);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo1, "B.java", newClass, bar2);
        AddParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, addParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getDestinationMethodSignature().equalsSignature(addParameterObject.getDestinationMethod()));
    }

}
