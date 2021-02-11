package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.GetDataForTests;
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

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);


        UndoOperations undo = new UndoOperations(project);

        undo.undoRenameMethod(ref);
        // Check that it matches the expected result
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFiles[0];
        PsiClass oldClass = psiJavaFile.getClasses()[0];
       // PsiClass oldClass = myFixture.findClass("renameTestData.methodRenameTestData.MethodRenameTestData");
        PsiMethod[] oldMethods = oldClass.getMethods();

        // Search for the java file in the project
        PsiJavaFile pFile = (PsiJavaFile) psiFiles[1];
        PsiClass newClass = pFile.getClasses()[0];
        PsiMethod[] newMethods = newClass.getMethods();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        for(int i = 0; i < newMethods.length; i++) {
            list1.add(oldMethods[i].getName());
            list2.add(newMethods[i].getName());
        }
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

}
