package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenamePackageObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenameParameterRenamePackageTest extends LightJavaCodeInsightFixtureTestCase {

    public void testCheckCombination() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");
        // Rename parameter p.subP1.C.foo.number -> p.subP1.C.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject("p.subP1.C", "p.subP1.C", null, null, null, null);


        RenameParameterRenamePackageCell.checkCombination(renamePackageObject,renameParameterObject1);
        Assert.assertEquals(renameParameterObject1.getRefactoredClassName(), "p.subP2.C");
    }
    public void testCheckCombination2() {
        // p.subP1 -> p.subP2
        RenamePackageObject renamePackageObject = new RenamePackageObject("p.subP1", "p.subP2");
        // Rename parameter p.subP2.foo.number -> p.subP2.foo.number1
        RenameParameterObject renameParameterObject1 =
                new RenameParameterObject("p.subP2.C", "p.subP2.C",
                        null, null, null, null);

        RenameParameterRenamePackageCell.checkCombination(renamePackageObject,renameParameterObject1);
        Assert.assertEquals(renameParameterObject1.getOriginalClassName(), "p.subP1.C");
    }

}
