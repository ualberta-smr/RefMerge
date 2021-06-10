package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class RenameClassRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckRenameClassRenameClassNamingConflict() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring leftRefactoring = refactorings.get(0);
        Refactoring rightRefactoring = refactorings.get(2);
        Refactoring rightRefactoring2 = refactorings.get(1);
        RefactoringObject leftRefactoringObject = RefactoringObjectUtils.createRefactoringObject(leftRefactoring);
        RefactoringObject rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring);
        boolean isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring2);
        isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }

    public void testFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        RenameClassObject firstRefactoring = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Rename class B -> C
        RenameClassObject secondRefactoring = new RenameClassObject("B.java", "B",
                "C.java", "C");
        secondRefactoring.setDestinationClassName("C");
        // Rename class A -> C
        RenameClassObject expectedRefactoring = new RenameClassObject("A.java", "A",
                "C.java", "C");

        doRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testNotFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        RenameClassObject firstRefactoring = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Rename class C -> D
        RenameClassObject secondRefactoring = new RenameClassObject("C.java", "C",
                "D.java", "D");
        // Rename class A -> B
        RenameClassObject expectedRefactoring = new RenameClassObject("A.java", "A",
                "B.java", "B");

        doRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }

    private void doRenameClassRenameClassTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        boolean isTransitive = RenameClassRenameClassCell.checkRenameClassRenameClassTransitivity(firstRefactoring,
                secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);

        }
        else {
            Assert.assertFalse(isTransitive);
        }
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
        Assert.assertEquals(((RenameClassObject) firstRefactoring).getDestinationClassName(),
                ((RenameClassObject) firstRefactoring).getDestinationClassName());
        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), firstRefactoring.getOriginalFilePath());
        Assert.assertEquals(((RenameClassObject) expectedRefactoring).getOriginalClassName(),
                ((RenameClassObject) firstRefactoring).getOriginalClassName());
    }
}
