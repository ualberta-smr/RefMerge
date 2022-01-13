package ca.ualberta.cs.smr.utils;

import ca.ualberta.cs.smr.refmerge.utils.Utils;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import java.util.List;
import java.util.Objects;

public class UtilsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testGetPsiClassByFilePath() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/methodRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testFile ="MethodRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;

        PsiClass[] psiClasses = TestUtils.getPsiClassesFromFile(psiFiles[0]);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);

        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        String qualifiedClass = renamed.getClassName();
        String filePath = original.getLocationInfo().getFilePath();
        Utils utils = new Utils(project);
        PsiClass actualClass = utils.getPsiClassByFilePath(filePath, qualifiedClass);
        String name = actualClass.getQualifiedName();
        PsiClass expectedClass = null;
        for(PsiClass psiClass : psiClasses) {
            if(Objects.equals(psiClass.getQualifiedName(), name)) {
                expectedClass = psiClass;
            }
        }
        LightJavaCodeInsightFixtureTestCase.assertEquals(expectedClass, actualClass);
    }
}
