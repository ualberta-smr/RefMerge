package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.refactoringminer.api.Refactoring;

import java.util.List;

public class ReplayOperationsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testReplayRenameMethod() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/methodRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedReplayResults/";
        String testFile ="MethodRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;


        PsiMethod[] oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        PsiMethod[] newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[1]);

        List<String> list1 = TestUtils.getMethodNames(oldMethods);
        List<String> list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        ReplayOperations replay = new ReplayOperations(project);
        replay.replayRenameMethod(ref);


        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayRenameClass() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/classRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedReplayResults/";
        String testFile = "ClassRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;


        PsiClass[] oldClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        PsiClass[] newClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);

        List<String> list1 = TestUtils.getClassNames(oldClasses);
        List<String> list2 = TestUtils.getClassNames(newClasses);

        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        ReplayOperations replay = new ReplayOperations(project);
        replay.replayRenameClass(ref);

        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);

    }

    public void testReplayExtractMethod() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String testDir = "/extractTestData/extractMethod/";
        String sourceTestData = testDir + "original/";
        String resultTestData = testDir + "expectedReplayResults/";
        String testFile = "Main.java";
        String resultFile = "replayResults.java";
        PsiFile[] files = myFixture.configureByFiles(sourceTestData + testFile, resultTestData + resultFile);

        testDir = basePath + "/" + getTestDataPath() + testDir;
        String originalTestData = testDir + "original/";
        String refactoredTestData = testDir + "refactored/";

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalTestData, refactoredTestData);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        ReplayOperations replayOperations = new ReplayOperations(project);
        try {
            replayOperations.replayExtractMethod(ref);
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }

        PsiFile psiFile1 = files[0];
        PsiFile psiFile2 = files[1];
        String content1 = psiFile1.getText();
        String content2 = psiFile2.getText();
        LightJavaCodeInsightFixtureTestCase.assertEquals(content1, content2);
    }
}
