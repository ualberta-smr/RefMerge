package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.core.refactoringObjects.typeObjects.ParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class InlineMethodReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    public void testMoveRenameMethodReceiver() {
        List<ParameterObject> originalParameters = new ArrayList<>();
        originalParameters.add(new ParameterObject("int", "return"));
        originalParameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(originalParameters, "foo");
        MethodSignatureObject target = new MethodSignatureObject(originalParameters, "target");
        MethodSignatureObject renamed = new MethodSignatureObject(originalParameters, "renamed");

        // Rename Method Foo.target -> FooBar.renamed
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("Foo.java", "Foo", target,
                "FooBar.java", "FooBar", renamed);
        // Inline Method Foo.foo -> Foo.target
        InlineMethodObject inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        // Inline Method Foo.foo -> FooBar.renamed
        InlineMethodObject expectedMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "FooBar.java", "FooBar", renamed);
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        dispatcher.set(moveRenameMethodObject, getProject(), true);
        InlineMethodReceiver receiver = new InlineMethodReceiver();
        receiver.set(inlineMethodObject);
        dispatcher.dispatch(receiver);
        Assert.assertEquals(inlineMethodObject.getDestinationClassName(), expectedMethodObject.getDestinationClassName());
        Assert.assertTrue(inlineMethodObject
                .getDestinationMethodSignature().equalsSignature(expectedMethodObject.getDestinationMethodSignature()));
        inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", foo,
                "Foo.java", "Foo", target);
        receiver.set(inlineMethodObject);
        dispatcher.set(moveRenameMethodObject, getProject(), false);
        dispatcher.dispatch(receiver);
        Assert.assertEquals(inlineMethodObject.getDestinationClassName(), expectedMethodObject.getDestinationClassName());
        Assert.assertTrue(inlineMethodObject
                .getDestinationMethodSignature().equalsSignature(expectedMethodObject.getDestinationMethodSignature()));
        Assert.assertFalse(receiver.isConflicting);
        inlineMethodObject = new InlineMethodObject("Foo.java", "Foo", target,
                "Foo.java", "Foo", foo);
        receiver.set(inlineMethodObject);
        dispatcher.dispatch(receiver);
        Assert.assertTrue(receiver.isConflicting);

    }
}
