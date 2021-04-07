package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ExtractMethodReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    public void testRenameClassRenameMethodDependenceCell() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/extractMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/extractMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION", originalPath, refactoredPath);
        List<Refactoring> classRenameRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert extractMethodRefactorings != null;
        assert classRenameRefactorings != null;
        Node extractMethodNode = new Node(extractMethodRefactorings.get(1));
        Node renameClassNode = new Node(classRenameRefactorings.get(0));

        boolean isDependent = ExtractMethodRenameClassCell.extractMethodRenameClassDependenceCell(renameClassNode, extractMethodNode);
        Assert.assertTrue(isDependent);
    }

}
