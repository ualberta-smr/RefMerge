package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.ParameterObject;
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
                .inlineMethodMoveRenameMethodConflictCell(moveRenameMethodObject, inlineMethodObject);
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
                .inlineMethodMoveRenameMethodDependenceCell(moveRenameMethodObject, inlineMethodObject);
        Assert.assertTrue(isDependent);
    }
}
