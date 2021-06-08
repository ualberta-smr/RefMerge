package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.ExtractMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;
import java.util.Objects;

public class ExtractMethodRenameClassLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckExtractMethodRenameClassDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION", originalPath, refactoredPath);
        assert extractMethodRefactorings != null;
        extractMethodRefactorings.addAll(Objects.requireNonNull(GetDataForTests.getRefactorings("EXTRACT_AND_MOVE_OPERATION",
                originalPath, refactoredPath)));
        List<Refactoring> renameClassRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert renameClassRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(0));
        Node renameClassNode = new Node(renameClassRefactorings.get(0));
        boolean isDependent = ExtractMethodRenameClassCell.checkExtractMethodRenameClassDependence(renameClassNode, extractMethodNode);
        Assert.assertFalse(isDependent);
        extractMethodNode = new Node(extractMethodRefactorings.get(1));
        isDependent = ExtractMethodRenameClassCell.checkExtractMethodRenameClassDependence(renameClassNode, extractMethodNode);
        Assert.assertTrue(isDependent);
        extractMethodNode = new Node(extractMethodRefactorings.get(2));
        isDependent = ExtractMethodRenameClassCell.checkExtractMethodRenameClassDependence(renameClassNode, extractMethodNode);
        Assert.assertTrue(isDependent);
    }

    public void testCheckExtractMethodRenameClassCombination() {
        // Extract method A.efoo from A.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("A.java", "A", "foo",
                "A.java", "A", "efoo");
        // Rename Class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", "foo",
                "B.java", "B", "ebar");
        doExtractMethodRenameClassTest(renameClassObject, extractMethodObject, expectedRefactoring);
    }

    public void testCeckExtractMethodRenameClassCombination2() {
        // Extract method B.efoo from B.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("B.java", "B", "foo",
                "B.java", "B", "efoo");
        // Rename Class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("A.java", "A", "foo",
                "B.java", "B", "ebar");
        doExtractMethodRenameClassTest(renameClassObject, extractMethodObject, expectedRefactoring);
    }

    public void testCeckExtractMethodRenameClassCombinationInSameFile() {
        // Extract method B.efoo from B.foo
        ExtractMethodObject extractMethodObject = new ExtractMethodObject("Foo.java", "B", "foo",
                "Foo.java", "B", "efoo");
        // Rename Class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("Foo.java", "A",
                "Foo.java", "B");
        // Extract method B.ebar from A.foo
        ExtractMethodObject expectedRefactoring = new ExtractMethodObject("Foo.java", "A", "foo",
                "Foo.java", "B", "ebar");
        doExtractMethodRenameClassTest(renameClassObject, extractMethodObject, expectedRefactoring);
    }


    private void doExtractMethodRenameClassTest(RefactoringObject renameClassObject, RefactoringObject extractMethodObject,
                                                RefactoringObject expectedRefactoring) {

        ExtractMethodRenameClassCell.checkExtractMethodRenameClassCombination(renameClassObject, extractMethodObject);

        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), extractMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), extractMethodObject.getDestinationFilePath());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getOriginalClassName(),
                ((ExtractMethodObject) extractMethodObject).getOriginalClassName());
        Assert.assertEquals(((ExtractMethodObject) expectedRefactoring).getDestinationClassName(),
                ((ExtractMethodObject) extractMethodObject).getDestinationClassName());

    }
}
