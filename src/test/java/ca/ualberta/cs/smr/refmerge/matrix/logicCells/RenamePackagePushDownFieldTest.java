package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.PushDownFieldObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackagePushDownFieldTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.f1 pushed down to p.subP1.f2
        PushDownFieldObject pushDownField = new PushDownFieldObject("p.subP1.C1",
                null, "p.subP1.C1", null);

        boolean isCombination = RenamePackagePushDownFieldCell.checkCombination(pushDownField, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pushDownField.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pushDownField.getTargetSubClass());
        // p.subP3.f1 pushed down to p.subP1.f2
        pushDownField = new PushDownFieldObject("p.subP1.C1",
                null, "p.subP3.C1", null);

        isCombination = RenamePackagePushDownFieldCell.checkCombination(pushDownField, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertEquals("p.subP2.C1", pushDownField.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pushDownField.getTargetSubClass());
        // p.subP1.f1 pushed down to p.subP3.f2
        pushDownField = new PushDownFieldObject("p.subP3.C1",
                null, "p.subP1.C1", null);

        isCombination = RenamePackagePushDownFieldCell.checkCombination(pushDownField, renamePackageObject);
        Assert.assertTrue(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pushDownField.getOriginalClass());
        Assert.assertEquals("p.subP2.C1", pushDownField.getTargetSubClass());

        isCombination = RenamePackagePushDownFieldCell.checkCombination(pushDownField, renamePackageObject);
        // p.subP3.f1 pushed down to p.subP3.f2
        pushDownField = new PushDownFieldObject("p.subP3.C1",
                null, "p.subP1.C3", null);

        Assert.assertFalse(isCombination);
        Assert.assertNotEquals("p.subP2.C1", pushDownField.getOriginalClass());
        Assert.assertNotEquals("p.subP2.C1", pushDownField.getTargetSubClass());

    }
}
