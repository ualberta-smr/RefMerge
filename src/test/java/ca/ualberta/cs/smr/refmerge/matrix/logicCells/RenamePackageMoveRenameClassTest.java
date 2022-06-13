package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Rename package p.subP2 -> p.subP3
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP2", "p.subP3");
        // Rename+Move class p.subP.C1 -> p.subP2.C2
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject("", "C1",
                "p.subP", "", "C2", "p.subP2");

        boolean isCombination = RenamePackageMoveRenameClassCell.checkCombination(moveRenameClassObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals(moveRenameClassObject.getDestinationClassObject().getPackageName(), "p.subP3");
    }
}
