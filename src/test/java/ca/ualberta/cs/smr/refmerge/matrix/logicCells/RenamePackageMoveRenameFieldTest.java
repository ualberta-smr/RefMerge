package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageMoveRenameFieldTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Rename package p.subP -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP", "p.subP2");
        // Rename field p.subP.C1.f1 -> p.subP.C1.f2
        MoveRenameFieldObject moveRenameMethodObject = new MoveRenameFieldObject("", "p.subP.C1",
                "f1", "", "p.subP.C1", "f2");

        boolean isCombination = RenamePackageMoveRenameFieldCell.checkCombination(moveRenameMethodObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals(moveRenameMethodObject.getDestinationClass(), "p.subP2.C1");
    }
}
