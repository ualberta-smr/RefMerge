package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackagePullUpMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.m1 pulled up to p.subP1.m2
        PullUpMethodObject pullUpMethodObject = new PullUpMethodObject("p.subP1.C1",
                null, "p.subP1.C1", null);

        boolean isCombination = RenamePackagePullUpMethodCell.checkCombination(pullUpMethodObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pullUpMethodObject.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pullUpMethodObject.getTargetClass());
        // p.subP3.m1 pulled up to p.subP1.m2
        pullUpMethodObject = new PullUpMethodObject("p.subP1.C1",
                null, "p.subP3.C1", null);

        isCombination = RenamePackagePullUpMethodCell.checkCombination(pullUpMethodObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pullUpMethodObject.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pullUpMethodObject.getTargetClass());
        // p.subP1.m1 pulled up to p.subP3.m2
        pullUpMethodObject = new PullUpMethodObject("p.subP3.C1",
                null, "p.subP1.C1", null);

        isCombination = RenamePackagePullUpMethodCell.checkCombination(pullUpMethodObject, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pullUpMethodObject.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pullUpMethodObject.getTargetClass());

        isCombination = RenamePackagePullUpMethodCell.checkCombination(pullUpMethodObject, renamePackageObject);
        // p.subP3.m1 pulled up to p.subP3.m2
        pullUpMethodObject = new PullUpMethodObject("p.subP3.C1",
                null, "p.subP1.C3", null);

        Assert.assertFalse(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pullUpMethodObject.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pullUpMethodObject.getTargetClass());

    }
}
