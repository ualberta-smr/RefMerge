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

public class RenameClassRenameMethodLogicTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testCheckRenameMethodRenameClassDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring methodRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring classRef = classRefs.get(0);
        Node classNode = new Node(classRef);
        Node methodNode = new Node(methodRef);
        boolean isDependent = RenameClassRenameMethodCell.checkRenameMethodRenameClassDependence(methodNode, classNode);
        Assert.assertTrue(isDependent);
    }

    public void testFoundRenameClassRenameMethodCombination() {
        // Rename class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Rename method A.foo -> B.bar
        RenameMethodObject renameMethodObject = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedMethodObject = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");

        doRenameClassRenameMethodTest(renameMethodObject, renameClassObject, expectedMethodObject);
    }

    public void testFoundRenameClassRenameMethodCombination2() {
        // Rename class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Rename method A.foo -> A.bar
        RenameMethodObject renameMethodObject = new RenameMethodObject("A.java", "A",
                "foo", "A.java", "A", "bar");
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedMethodObject = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");
        doRenameClassRenameMethodTest(renameMethodObject, renameClassObject, expectedMethodObject);
    }

    public void testFoundRenameClassRenameMethodCombination3() {
        // Rename class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // Rename method B.foo -> B.bar
        RenameMethodObject renameMethodObject = new RenameMethodObject("B.java", "B",
                "foo", "B.java", "B", "bar");
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedMethodObject = new RenameMethodObject("A.java", "A",
                "foo", "B.java", "B", "bar");
        doRenameClassRenameMethodTest(renameMethodObject, renameClassObject, expectedMethodObject);
    }


    public void testNotFoundRenameClassRenameMethodCombination() {
        // Rename class A -> B
        RenameClassObject renameClassObject = new RenameClassObject("A.java", "A",
                "b.java", "B");
        // Rename method C.foo -> C.bar
        RenameMethodObject renameMethodObject = new RenameMethodObject("C.java", "C", "foo",
                "C.java", "C", "bar");
        // Rename Method C.foo -> C.bar
        RenameMethodObject expectedMethodObject = new RenameMethodObject("C.java", "C", "foo",
                "C.java", "C", "bar");
        doRenameClassRenameMethodTest(renameMethodObject, renameClassObject, expectedMethodObject);
    }

    private void doRenameClassRenameMethodTest(RefactoringObject renameMethodObject, RefactoringObject renameClassObject,
                                               RefactoringObject expectedRefactoring) {
        RenameClassRenameMethodCell.checkRenameClassRenameMethodCombination(renameMethodObject, renameClassObject);

        Assert.assertEquals(expectedRefactoring.getOriginalFilePath(), renameMethodObject.getOriginalFilePath());
        Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), renameMethodObject.getDestinationFilePath());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationClassName(),
                ((RenameMethodObject) renameMethodObject).getDestinationClassName());
        Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getOriginalClassName(),
                ((RenameMethodObject) renameMethodObject).getOriginalClassName());
    }

}
