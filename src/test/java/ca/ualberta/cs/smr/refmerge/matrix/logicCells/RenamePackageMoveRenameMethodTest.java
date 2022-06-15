package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageMoveRenameMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // Rename package p.subP -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP", "p.subP2");
        // Rename method p.subP.C1.m1 -> p.subP.C1.m2
        MoveRenameMethodObject moveRenameMethodObject = new MoveRenameMethodObject("", "p.subP.C1",
                null, "", "p.subP.C1", null);

        RenamePackageMoveRenameMethodCell.checkCombination(moveRenameMethodObject, renamePackageObject);
        Assert.assertEquals(moveRenameMethodObject.getDestinationClassName(), "p.subP2.C1");
    }

}
