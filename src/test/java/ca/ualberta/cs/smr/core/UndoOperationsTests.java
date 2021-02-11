package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.*;
import org.refactoringminer.api.Refactoring;

import java.util.*;


public class UndoOperationsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }


    public void testUndoRenameMethod() {
        Project project = myFixture.getProject();
        String testDataRenamed = "renameTestData/methodRenameTestData/renamed/";
        String testDataOriginal = "renameTestData/methodRenameTestData/original/";
        String testResult = "renameTestData/methodRenameTestData/expectedResults/";
        String testFile ="MethodRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile, testResult + testFile);
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
        UndoOperations undo = new UndoOperations(project);

        undo.undoRenameMethod(ref);

        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

}
