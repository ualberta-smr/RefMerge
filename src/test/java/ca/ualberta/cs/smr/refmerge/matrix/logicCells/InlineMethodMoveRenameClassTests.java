package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class InlineMethodMoveRenameClassTests extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckInlineMethodMoveRenameClassDependence() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");

        // Rename Class Foo -> Bar
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("Foo.java", "Foo", "package",
                "Bar.java", "Bar", "package");
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        boolean isDependent = InlineMethodMoveRenameClassCell
                .checkDependence(moveRenameClassObject, inlineMethodObject);
        Assert.assertTrue(isDependent);
    }

    public void testCheckInlineMethodMoveRenameClassCombination() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");

        // Rename Class Foo -> Bar
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("Foo.java", "Foo", "package",
                "Bar.java", "Bar", "package");
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObjectBefore = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        // Inline Method Bar.foo -> Bar.target
        InlineMethodObject inlineMethodObjectAfter = new InlineMethodObject("Bar.java", "Bar", foo,
                "Bar.java", "Bar", target);
        InlineMethodMoveRenameClassCell.checkCombination(moveRenameClassObject, inlineMethodObjectBefore);
        InlineMethodMoveRenameClassCell.checkCombination(moveRenameClassObject, inlineMethodObjectAfter);
        Assert.assertEquals(inlineMethodObjectAfter.getOriginalClassName(), inlineMethodObjectBefore.getOriginalClassName());
        Assert.assertEquals(inlineMethodObjectAfter.getDestinationClassName(), inlineMethodObjectBefore.getDestinationClassName());
    }
}
