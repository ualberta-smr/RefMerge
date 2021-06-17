package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.ParameterObject;
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
}
