package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RenameFieldDispatcher;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.RefactoringType;

public class MoveRenameFieldReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testRenameFieldRenameFieldTransitivity() {
        Project project = myFixture.getProject();
        // A.foo -> A.bar
        MoveRenameFieldObject leftRenameFieldObject = new MoveRenameFieldObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        leftRenameFieldObject.setType(RefactoringType.RENAME_ATTRIBUTE);
        // A.bar -> A.foobar
        MoveRenameFieldObject rightRenameFieldObject1 = new MoveRenameFieldObject("A.java", "A",
                "bar", "A.java", "A", "foobar");
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);
        rightRenameFieldObject1.setType(RefactoringType.RENAME_ATTRIBUTE);
        RenameFieldDispatcher dispatcher = new RenameFieldDispatcher();
        dispatcher.set(rightRenameFieldObject1, project, true);
        MoveRenameFieldReceiver receiver = new MoveRenameFieldReceiver();
        receiver.set(leftRenameFieldObject, project);
        dispatcher.dispatch(receiver);

        Assert.assertTrue(receiver.isTransitive);
        Assert.assertEquals(leftRenameFieldObject.getDestinationName(), rightRenameFieldObject1.getDestinationName());

    }


}
