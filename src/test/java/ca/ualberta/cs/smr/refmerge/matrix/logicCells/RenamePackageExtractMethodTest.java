package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenamePackageExtractMethodTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");

        // p.subP1.m1 extracted from p.subP1.m2
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("", "p.subP1.C1",
                null, "", "p.subP1.C1", null);

        RenamePackageExtractMethodCell.checkCombination(extractMethodObject, renamePackageObject);
        Assert.assertEquals("p.subP2.C1", extractMethodObject.getOriginalClassName());
        Assert.assertEquals("p.subP2.C1", extractMethodObject.getDestinationClassName());
        // p.subP3.m1 extracted from p.subP1.m2
        extractMethodObject = new ExtractMethodObject("", "p.subP1.C1",
                null, "", "p.subP3.C1", null);

        RenamePackageExtractMethodCell.checkCombination(extractMethodObject, renamePackageObject);

        Assert.assertEquals("p.subP2.C1", extractMethodObject.getOriginalClassName());
        Assert.assertNotEquals("p.subP2.C1", extractMethodObject.getDestinationClassName());
        // p.subP1.m1 extracted from p.subP3.m2
        extractMethodObject = new ExtractMethodObject("", "p.subP3.C1",
                null, "", "p.subP1.C1", null);

        RenamePackageExtractMethodCell.checkCombination(extractMethodObject, renamePackageObject);

        Assert.assertNotEquals("p.subP2.C1", extractMethodObject.getOriginalClassName());
        Assert.assertEquals("p.subP2.C1", extractMethodObject.getDestinationClassName());

        RenamePackageExtractMethodCell.checkCombination(extractMethodObject, renamePackageObject);
        // p.subP3.m1 extracted from p.subP3.m2
        extractMethodObject = new ExtractMethodObject("", "p.subP3.C1",
                null, "", "p.subP1.C3", null);

        Assert.assertNotEquals("p.subP2.C1", extractMethodObject.getOriginalClassName());
        Assert.assertNotEquals("p.subP2.C1", extractMethodObject.getDestinationClassName());

    }

}
