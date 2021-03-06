package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageRenamePackageTest extends LightJavaCodeInsightFixtureTestCase {

    public void testNamingConflict() {
        // Rename package A -> B
        RenamePackageObject receiver = new RenamePackageObject("A", "B");
        // Rename package A -> B
        RenamePackageObject dispatcher = new RenamePackageObject("A", "B");

        boolean isConflicting = RenamePackageRenamePackageCell.conflictCell(receiver, dispatcher);
        Assert.assertFalse(isConflicting);

        // Rename package A -> C (conflicts)
        receiver = new RenamePackageObject("A", "C");
        isConflicting = RenamePackageRenamePackageCell.conflictCell(receiver, dispatcher);
        Assert.assertTrue(isConflicting);

        // Rename package B -> C (conflicts)
        receiver = new RenamePackageObject("C", "B");
        isConflicting = RenamePackageRenamePackageCell.conflictCell(receiver, dispatcher);
        Assert.assertTrue(isConflicting);

    }

    public void testTransitivity() {
        // Rename package A -> B
        RenamePackageObject receiver = new RenamePackageObject("A", "B");
        // Rename package B -> C
        RenamePackageObject dispatcher = new RenamePackageObject("B", "C");

        boolean isTransitive = RenamePackageRenamePackageCell.checkTransitivity(receiver, dispatcher);
        Assert.assertTrue(isTransitive);
        Assert.assertEquals(receiver.getDestinationName(), dispatcher.getDestinationName());
        Assert.assertEquals(dispatcher.getOriginalName(), receiver.getOriginalName());

        // Rename package A -> B
        receiver = new RenamePackageObject("A", "B");
        // Rename package B -> C
        dispatcher = new RenamePackageObject("B", "C");

        isTransitive = RenamePackageRenamePackageCell.checkTransitivity(dispatcher, receiver);
        Assert.assertFalse(isTransitive);
    }

}
