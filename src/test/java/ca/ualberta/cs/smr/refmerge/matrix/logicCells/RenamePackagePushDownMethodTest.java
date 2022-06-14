package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackagePushDownMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.m1 pushed down to p.subP1.m2
        PushDownMethodObject pushDownMethod = new PushDownMethodObject("p.subP1.C1",
                null, "p.subP1.C1", null);

        RenamePackagePushDownMethodCell.checkCombination(pushDownMethod, renamePackageObject);

        Assert.assertEquals("p.subP2.C1", pushDownMethod.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pushDownMethod.getTargetBaseClass());
        // p.subP3.m1 pushed down to p.subP1.m2
        pushDownMethod = new PushDownMethodObject("p.subP1.C1",
                null, "p.subP3.C1", null);

        RenamePackagePushDownMethodCell.checkCombination(pushDownMethod, renamePackageObject);

        Assert.assertEquals("p.subP2.C1", pushDownMethod.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pushDownMethod.getTargetBaseClass());
        // p.subP1.m1 pushed down to p.subP3.m2
        pushDownMethod = new PushDownMethodObject("p.subP3.C1",
                null, "p.subP1.C1", null);

        RenamePackagePushDownMethodCell.checkCombination(pushDownMethod, renamePackageObject);

        Assert.assertNotEquals("p.subP2.C1", pushDownMethod.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pushDownMethod.getTargetBaseClass());

        RenamePackagePushDownMethodCell.checkCombination(pushDownMethod, renamePackageObject);
        // p.subP3.m1 pushed down to p.subP3.m2
        pushDownMethod = new PushDownMethodObject("p.subP3.C1",
                null, "p.subP1.C3", null);

        Assert.assertNotEquals("p.subP2.C1", pushDownMethod.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pushDownMethod.getTargetBaseClass());

    }
}
