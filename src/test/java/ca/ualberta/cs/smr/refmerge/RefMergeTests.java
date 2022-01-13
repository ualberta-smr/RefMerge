package ca.ualberta.cs.smr.refmerge;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.replayOperations.ReplayRefactorings;
import ca.ualberta.cs.smr.refmerge.invertOperations.InvertRefactorings;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.ArrayList;
import java.util.List;

public class RefMergeTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testUndoRefactorings() {
        Project project = myFixture.getProject();
        String testDir = "refMergeTestData/refactorings/";
        String testDataRenamed = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedUndoResults/";
        String testFile = "RefactoringTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;

        PsiMethod[] oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        PsiMethod[] newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[1]);
        List<String> list1 = TestUtils.getMethodNames(oldMethods);
        List<String> list2 = TestUtils.getMethodNames(newMethods);
        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        PsiClass[] oldClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        PsiClass[] newClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);
        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);
        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<RefactoringObject> classRefactorings = GetDataForTests.getRefactoringObjects("RENAME_CLASS", originalPath, refactoredPath);
        List<RefactoringObject> methodRefactorings = GetDataForTests.getRefactoringObjects("RENAME_METHOD", originalPath, refactoredPath);
        assert classRefactorings != null && methodRefactorings != null;
        ArrayList<RefactoringObject> refactorings = new ArrayList<>();
        refactorings.addAll(classRefactorings);
        refactorings.addAll(methodRefactorings);
        InvertRefactorings.invertRefactorings(refactorings, project);

        oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[1]);
        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);

        oldClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        newClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);
        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayRefactorings() {
        Project project = myFixture.getProject();
        String testDir = "refMergeTestData/refactorings/";
        String testDataRenamed = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedReplayResults/";
        String testFile = "RefactoringTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;

        PsiMethod[] oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        PsiMethod[] newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[1]);
        List<String> list1 = TestUtils.getMethodNames(oldMethods);
        List<String> list2 = TestUtils.getMethodNames(newMethods);
        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        PsiClass[] oldClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        PsiClass[] newClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);
        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);
        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<RefactoringObject> classRefactorings = GetDataForTests.getRefactoringObjects("RENAME_CLASS", originalPath, refactoredPath);
        List<RefactoringObject> methodRefactorings = GetDataForTests.getRefactoringObjects("RENAME_METHOD", originalPath, refactoredPath);
        assert classRefactorings != null && methodRefactorings != null;
        ArrayList<RefactoringObject> refactorings = new ArrayList<>();
        refactorings.addAll(methodRefactorings);
        refactorings.addAll(classRefactorings);
        ReplayRefactorings.replayRefactorings(refactorings, project);

        oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[1]);
        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);

        oldClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);
        newClasses = TestUtils.getPsiClassesFromFile(psiFiles[1]);
        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }
}
