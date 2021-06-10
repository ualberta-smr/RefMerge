package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameClassCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ExtractMethodReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    public void testRenameClassRenameMethodDependenceCell() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/extractMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> extractMethodRefactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION", originalPath, refactoredPath);
        List<Refactoring> classRenameRefactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert extractMethodRefactorings != null;
        assert classRenameRefactorings != null;
        RefactoringObject extractMethodObject = RefactoringObjectUtils.createRefactoringObject(extractMethodRefactorings.get(1));
        RefactoringObject renameClassObject = RefactoringObjectUtils.createRefactoringObject(classRenameRefactorings.get(0));

        boolean isDependent = ExtractMethodRenameClassCell.extractMethodRenameClassDependenceCell(renameClassObject, extractMethodObject);
        Assert.assertTrue(isDependent);
    }

}
