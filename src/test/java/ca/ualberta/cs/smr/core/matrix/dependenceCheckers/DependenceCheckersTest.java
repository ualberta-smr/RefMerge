package ca.ualberta.cs.smr.core.matrix.dependenceCheckers;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class DependenceCheckersTest {

    @Test
    public void checkRenameMethodRenameClassDependenceTest() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring methodRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring classRef = classRefs.get(0);
        boolean isDependent = DependenceCheckers.checkRenameMethodRenameClassDependence(classRef, methodRef);
        Assert.assertTrue(isDependent);
    }
}
