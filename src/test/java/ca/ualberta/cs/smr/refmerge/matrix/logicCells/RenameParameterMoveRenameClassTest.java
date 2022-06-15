package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;

public class RenameParameterMoveRenameClassTest extends LightJavaCodeInsightFixtureTestCase {

    /*
     * Rename parameter happens before move + rename class
     */
    public void testCheckCombination() {
        String originalFilePath = "A.java";
        String originalClass = "A";
        String originalPackage = "package";
        String destinationPath = "B.java";
        String destinationClass = "B";
        String destinationPackage = "newPackage";
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject(originalFilePath, originalClass, originalPackage,
                destinationPath, destinationClass, destinationPackage);

        RenameParameterObject renameParameterObject =
                new RenameParameterObject("package.A", "package.A", null,
                        null, null, null);

        RenameParameterMoveRenameClassCell.checkCombination(moveRenameClassObject, renameParameterObject);
        Assert.assertEquals(moveRenameClassObject.getDestinationFilePath(), renameParameterObject.getDestinationFilePath());
        String destinationClassName = moveRenameClassObject.getDestinationClassObject().getPackageName() + "."
                + moveRenameClassObject.getDestinationClassObject().getClassName();
        Assert.assertEquals(destinationClassName, renameParameterObject.getRefactoredClassName());
    }

    /*
     * Rename parameter happens after move + rename class
     */
    public void testCheckCombination2() {
        String originalFilePath = "A.java";
        String originalClass = "A";
        String originalPackage = "package";
        String destinationPath = "B.java";
        String destinationClass = "B";
        String destinationPackage = "newPackage";
        MoveRenameClassObject moveRenameClassObject = new MoveRenameClassObject(originalFilePath, originalClass, originalPackage,
                destinationPath, destinationClass, destinationPackage);

        RenameParameterObject renameParameterObject =
                new RenameParameterObject("newPackage.B", "newPackage.B", null,
                        null, null, null);
        RenameParameterMoveRenameClassCell.checkCombination(moveRenameClassObject, renameParameterObject);
        Assert.assertEquals(moveRenameClassObject.getOriginalFilePath(), renameParameterObject.getOriginalFilePath());
        String originalClassName = moveRenameClassObject.getOriginalClassObject().getPackageName() + "."
                + moveRenameClassObject.getOriginalClassObject().getClassName();
        Assert.assertEquals(originalClassName, renameParameterObject.getOriginalClassName());
    }

}
