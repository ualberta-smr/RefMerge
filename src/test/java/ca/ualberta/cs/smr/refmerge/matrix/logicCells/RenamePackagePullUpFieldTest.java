package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackagePullUpFieldTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.f1 pulled up to p.subP1.f2
        PullUpFieldObject pullUpFieldObject = new PullUpFieldObject("p.subP1.C1",
                null, "p.subP1.C1", null);

        boolean isCombination = RenamePackagePullUpFieldCell.checkCombination(pullUpFieldObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pullUpFieldObject.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pullUpFieldObject.getTargetClass());
        // p.subP3.f1 pulled up to p.subP1.f2
        pullUpFieldObject = new PullUpFieldObject("p.subP1.C1",
                null, "p.subP3.C1", null);

        isCombination = RenamePackagePullUpFieldCell.checkCombination(pullUpFieldObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pullUpFieldObject.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pullUpFieldObject.getTargetClass());
        // p.subP1.f1 pulled up to p.subP3.f2
        pullUpFieldObject = new PullUpFieldObject("p.subP3.C1",
                null, "p.subP1.C1", null);

        isCombination = RenamePackagePullUpFieldCell.checkCombination(pullUpFieldObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pullUpFieldObject.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pullUpFieldObject.getTargetClass());

        isCombination = RenamePackagePullUpFieldCell.checkCombination(pullUpFieldObject, renamePackageObject);
        // p.subP3.f1 pulled up to p.subP3.f2
        pullUpFieldObject = new PullUpFieldObject("p.subP3.C1",
                null, "p.subP1.C3", null);

        Assert.assertFalse(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pullUpFieldObject.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pullUpFieldObject.getTargetClass());

    }


}
