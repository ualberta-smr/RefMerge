package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.*;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.List;

public class MoveRenameClassMoveRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

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
        boolean isConflicting = MoveRenameClassMoveRenameClassCell.checkClassNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        rightRefactoringObject = RefactoringObjectUtils.createRefactoringObject(rightRefactoring2);
        isConflicting = MoveRenameClassMoveRenameClassCell.checkClassNamingConflict(leftRefactoringObject, rightRefactoringObject);
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }

    public void testMoveRenameMethodDependence() {
        // Rename class original.A -> original.B
        MoveRenameClassObject renameClass = new MoveRenameClassObject("A.java", "A", "original",
                "B.java", "B", "original");
        renameClass.setType(RefactoringType.RENAME_CLASS);
        // Move class original.A -> destination.B
        MoveRenameClassObject moveClass = new MoveRenameClassObject("A.java", "A", "original",
                "C.java", "C", "destination");
        moveClass.setType(RefactoringType.MOVE_CLASS);
        // Rename+Move class original.A -> destination.B
        MoveRenameClassObject moveRenameClass = new MoveRenameClassObject("A.java", "A", "original",
                "C.java", "C", "destination2");
        moveRenameClass.setType(RefactoringType.MOVE_RENAME_CLASS);

        boolean isDependent = MoveRenameClassMoveRenameClassCell.checkMoveRenameClassMoveRenameClassDependence(renameClass, moveClass);
        Assert.assertTrue(isDependent);
        isDependent = MoveRenameClassMoveRenameClassCell.checkMoveRenameClassMoveRenameClassDependence(moveClass, renameClass);
        Assert.assertTrue(isDependent);
        isDependent = MoveRenameClassMoveRenameClassCell.checkMoveRenameClassMoveRenameClassDependence(moveRenameClass, moveClass);
        Assert.assertFalse(isDependent);
    }

    public void testFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        MoveRenameClassObject firstRefactoring = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Rename class B -> C
        MoveRenameClassObject secondRefactoring = new MoveRenameClassObject("B.java", "B", "package",
                "C.java", "C", "package");
        // Rename class A -> C
        MoveRenameClassObject expectedRefactoring = new MoveRenameClassObject("A.java", "A", "package",
                "C.java", "C", "package");

        doMoveRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testFoundRenameClassMoveClassTransitivity() {
        // Rename class original.A -> original.B
        MoveRenameClassObject firstRefactoring = new MoveRenameClassObject("A.java", "A", "original",
                "B.java", "B", "original");
        // Move class original.B -> destination.B
        MoveRenameClassObject secondRefactoring = new MoveRenameClassObject("B.java", "B", "original",
                "B.java", "B", "destination");
        // Rename+Move class original.A -> destination.B
        MoveRenameClassObject expectedRefactoring = new MoveRenameClassObject("A.java", "A", "original",
                "B.java", "B", "destination");

        doMoveRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testFoundMoveRenameClassMoveRenameClassTransitivity() {
        // Rename+Move class original.A -> destination1.B
        MoveRenameClassObject firstRefactoring = new MoveRenameClassObject("A.java", "A", "original",
                "B.java", "B", "destination1");
        // Rename+Move class destination1.B -> destination2.C
        MoveRenameClassObject secondRefactoring = new MoveRenameClassObject("B.java", "B", "destination1",
                "C.java", "C", "destination2");
        // Rename+Move class original.A -> destination.B
        MoveRenameClassObject expectedRefactoring = new MoveRenameClassObject("A.java", "A", "original",
                "C.java", "C", "destination2");

        doMoveRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testNotFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        MoveRenameClassObject firstRefactoring = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // Rename class C -> D
        MoveRenameClassObject secondRefactoring = new MoveRenameClassObject("C.java", "C", "package",
                "D.java", "D", "package");
        // Rename class A -> B
        MoveRenameClassObject expectedRefactoring = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");

        doMoveRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }




    private void doMoveRenameClassRenameClassTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                  RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        boolean isTransitive = MoveRenameClassMoveRenameClassCell.checkMoveRenameClassMoveRenameClassTransitivity(firstRefactoring,
                secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);

        }
        else {
            Assert.assertFalse(isTransitive);
        }
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
        Assert.assertEquals(((MoveRenameClassObject) firstRefactoring).getDestinationClassObject(),
                ((MoveRenameClassObject) firstRefactoring).getDestinationClassObject());
        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), firstRefactoring.getOriginalFilePath());
        Assert.assertEquals(((MoveRenameClassObject) expectedRefactoring).getOriginalClassObject().getClassName(),
                ((MoveRenameClassObject) firstRefactoring).getOriginalClassObject().getClassName());
        Assert.assertEquals(((MoveRenameClassObject) expectedRefactoring).getOriginalClassObject().getPackageName(),
                ((MoveRenameClassObject) firstRefactoring).getOriginalClassObject().getPackageName());
        Assert.assertEquals(((MoveRenameClassObject) expectedRefactoring).getDestinationClassObject().getClassName(),
                ((MoveRenameClassObject) firstRefactoring).getDestinationClassObject().getClassName());
        Assert.assertEquals(((MoveRenameClassObject) expectedRefactoring).getDestinationClassObject().getPackageName(),
                ((MoveRenameClassObject) firstRefactoring).getDestinationClassObject().getPackageName());
    }
}
