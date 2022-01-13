package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class InlineMethodMoveRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckInlineMethodMoveRenameMethodConflict() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");
        MethodSignatureObject renamed = new MethodSignatureObject(originalParameters, "renamed");

        // Rename Method Foo.foo -> Foo.renamed
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", renamed);
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        boolean isConflicting = InlineMethodMoveRenameMethodCell
                .conflictCell(moveRenameMethodObject, inlineMethodObject);
        Assert.assertTrue(isConflicting);
    }

    public void testCheckInlineMethodMoveRenameMethodDependence() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");
        MethodSignatureObject renamed = new MethodSignatureObject(originalParameters, "renamed");

        // Rename Method Foo.foo -> Foo.renamed
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("Foo.java", "Foo", target,
                "Foo.java", "Foo", renamed);
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        boolean isDependent = InlineMethodMoveRenameMethodCell
                .dependenceCell(moveRenameMethodObject, inlineMethodObject);
        Assert.assertTrue(isDependent);
    }

    public void testCheckInlineMethodMoveRenameMethodCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");
        MethodSignatureObject renamed = new MethodSignatureObject(originalParameters, "renamed");

        // Rename Method Foo.foo -> Foo.renamed
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("Foo.java", "Foo", target,
                "FooBar.java", "FooBar", renamed);
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject expectedMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "FooBar.java", "FooBar", target);
        boolean hasCombination = InlineMethodMoveRenameMethodCell
                .checkCombination(moveRenameMethodObject, inlineMethodObject);
        Assert.assertTrue(hasCombination);
        Assert.assertEquals(inlineMethodObject.getDestinationClassName(), moveRenameMethodObject.getDestinationClassName());
        Assert.assertTrue(inlineMethodObject
                .getDestinationMethodSignature().equalsSignature(moveRenameMethodObject.getDestinationMethodSignature()));
    }
}
