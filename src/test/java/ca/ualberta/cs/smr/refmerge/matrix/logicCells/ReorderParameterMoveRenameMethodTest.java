package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.ReorderParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ReorderParameterMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Reorder parameters before move + rename method
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
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList4, "bar");
        String originalClass = "A";
        String newClass = "B";
        ReorderParameterObject reorderParameterObject = new ReorderParameterObject(originalClass, originalClass, foo1, foo2);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo3, "B.java", newClass, bar4);
        ReorderParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, reorderParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getOriginalMethodSignature().equalsSignature(reorderParameterObject.getOriginalMethod()));
    }

    /*
     * Move + rename method before add parameter
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
        MethodSignatureObject bar2 = new MethodSignatureObject(parameterList2, "bar");
        parameterList.add(parameterObject);
        parameterList.add(parameterObject1);
        parameterList.add(parameterObject2);
        MethodSignatureObject bar3 = new MethodSignatureObject(parameterList3, "bar");
        parameterList4.add(parameterObject1);
        parameterList4.add(parameterObject2);
        parameterList4.add(parameterObject);
        MethodSignatureObject bar4 = new MethodSignatureObject(parameterList4, "bar");
        String originalClass = "A";
        String newClass = "B";
        ReorderParameterObject reorderParameterObject = new ReorderParameterObject(newClass, newClass, bar3, bar4);

        MoveRenameMethodObject moveRenameMethodObject =
                new MoveRenameMethodObject("A.java", originalClass, foo1, "B.java", newClass, bar2);
        ReorderParameterMoveRenameMethodCell.checkCombination(moveRenameMethodObject, reorderParameterObject);
        Assert.assertTrue(moveRenameMethodObject.getDestinationMethodSignature().equalsSignature(reorderParameterObject.getDestinationMethod()));
    }
}
