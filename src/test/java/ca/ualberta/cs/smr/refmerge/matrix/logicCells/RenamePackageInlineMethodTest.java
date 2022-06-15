package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageInlineMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.m1 inlined to p.subP1.m2
        InlineMethodObject inlineMethodObject = new InlineMethodObject("", "p.subP1.C1",
                null, "", "p.subP1.C1", null);

        RenamePackageInlineMethodCell.checkCombination(inlineMethodObject, renamePackageObject);
        Assert.assertEquals("p.subP2.C1", inlineMethodObject.getOriginalClassName());
        Assert.assertEquals("p.subP2.C1", inlineMethodObject.getDestinationClassName());
        // p.subP3.m1 inlined to p.subP1.m2
        inlineMethodObject = new InlineMethodObject("", "p.subP1.C1",
                null, "", "p.subP3.C1", null);

        RenamePackageInlineMethodCell.checkCombination(inlineMethodObject, renamePackageObject);

        Assert.assertEquals("p.subP2.C1", inlineMethodObject.getOriginalClassName());
        Assert.assertNotEquals("p.subP2.C1", inlineMethodObject.getDestinationClassName());
        // p.subP1.m1 inlined to p.subP3.m2
        inlineMethodObject = new InlineMethodObject("", "p.subP3.C1",
                null, "", "p.subP1.C1", null);

        RenamePackageInlineMethodCell.checkCombination(inlineMethodObject, renamePackageObject);
        Assert.assertNotEquals("p.subP2.C1", inlineMethodObject.getOriginalClassName());
        Assert.assertEquals("p.subP2.C1", inlineMethodObject.getDestinationClassName());

        RenamePackageInlineMethodCell.checkCombination(inlineMethodObject, renamePackageObject);
        // p.subP3.m1 inlined to p.subP3.m2
        inlineMethodObject = new InlineMethodObject("", "p.subP3.C1",
                null, "", "p.subP1.C3", null);

        Assert.assertNotEquals("p.subP2.C1", inlineMethodObject.getOriginalClassName());
        Assert.assertNotEquals("p.subP2.C1", inlineMethodObject.getDestinationClassName());

    }
}
