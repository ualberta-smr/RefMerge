package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
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
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        boolean isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(new Node(foo), new Node(bar));
        Assert.assertFalse("Classes without related refactorings should not conflict", isConflicting);
        isConflicting = RenameClassRenameClassCell.checkClassNamingConflict(new Node(foo), new Node(foo2));
        Assert.assertTrue("Classes renamed to the same name in the same package conflict", isConflicting);

    }

    public void testFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        RenameClassObject firstRefactoring = new RenameClassObject();
        firstRefactoring.setOriginalFilePath("A.java");
        firstRefactoring.setOriginalClassName("A");
        firstRefactoring.setDestinationFilePath("B.java");
        firstRefactoring.setDestinationClassName("B");
        // Rename class B -> C
        RenameClassObject secondRefactoring = new RenameClassObject();
        secondRefactoring.setOriginalFilePath("B.java");
        secondRefactoring.setOriginalClassName("B");
        secondRefactoring.setDestinationFilePath("C.java");
        secondRefactoring.setDestinationClassName("C");
        // Rename class A -> C
        RenameClassObject expectedRefactoring = new RenameClassObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setDestinationFilePath("C.java");
        expectedRefactoring.setDestinationClassName("C");

        doRenameClassRenameClassTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    public void testNotFoundRenameClassRenameClassTransitivity() {
        // Rename class A -> B
        RenameClassObject firstRefactoring = new RenameClassObject();
        firstRefactoring.setOriginalFilePath("A.java");
        firstRefactoring.setOriginalClassName("A");
        firstRefactoring.setDestinationFilePath("B.java");
        firstRefactoring.setDestinationClassName("B");
        // Rename class C -> D
        RenameClassObject secondRefactoring = new RenameClassObject();
        secondRefactoring.setOriginalFilePath("C.java");
        secondRefactoring.setOriginalClassName("C");
        secondRefactoring.setDestinationFilePath("D.java");
        secondRefactoring.setDestinationClassName("D");
        // Rename class A -> B
        RenameClassObject expectedRefactoring = new RenameClassObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setDestinationFilePath("B.java");
        expectedRefactoring.setDestinationClassName("B");

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
