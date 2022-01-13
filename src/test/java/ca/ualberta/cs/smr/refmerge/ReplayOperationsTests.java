package ca.ualberta.cs.smr.refmerge;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.refmerge.replayOperations.ReplayExtractMethod;
import ca.ualberta.cs.smr.refmerge.replayOperations.ReplayInlineMethod;
import ca.ualberta.cs.smr.refmerge.replayOperations.ReplayMoveRenameClass;
import ca.ualberta.cs.smr.refmerge.replayOperations.ReplayMoveRenameMethod;
import ca.ualberta.cs.smr.refmerge.invertOperations.InvertExtractMethod;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class ReplayOperationsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
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
        ReplayMoveRenameMethod replayMoveRenameMethod = new ReplayMoveRenameMethod(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        replayMoveRenameMethod.replayMoveRenameMethod(refactoringObject);


        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayMoveRenameMethod() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameMethod/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile = "Main.java";
        String testFile2 = "OtherClass.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testDataOriginal + testFile2,
                testDataRefactored + testFile, testDataRefactored + testFile2);

        PsiMethod[] oldMethods = TestUtils.getPsiMethodsFromFile(psiFiles[0]);
        PsiMethod[] newMethods = TestUtils.getPsiMethodsFromFile(psiFiles[2]);

        List<String> list1 = TestUtils.getMethodNames(oldMethods);
        List<String> list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<ParameterObject> fooParameters = new ArrayList<>();
        fooParameters.add(new ParameterObject("void", "return"));
        MethodSignatureObject foo = new MethodSignatureObject(fooParameters, "foo");
        List<ParameterObject> foobarParameters = new ArrayList<>();
        foobarParameters.add(new ParameterObject("int", "return"));
        foobarParameters.add(new ParameterObject("int", "x"));
        foobarParameters.add(new ParameterObject("double", "y"));
        MethodSignatureObject foobar = new MethodSignatureObject(foobarParameters, "foobar");
        List<ParameterObject> equalsParameters = new ArrayList<>();
        equalsParameters.add(new ParameterObject("boolean", "return"));
        equalsParameters.add(new ParameterObject("Object", "o1"));
        equalsParameters.add(new ParameterObject("Object", "o2"));
        MethodSignatureObject equals = new MethodSignatureObject(equalsParameters, "equals");
        MethodSignatureObject isEqual = new MethodSignatureObject(equalsParameters, "isEqual");
        // Move Main.foo -> OtherClass.foo
        MoveRenameMethodObject fooObject = new MoveRenameMethodObject("Main.java", "Main",
                foo, "OtherClass.java", "OtherClass", foo);
        fooObject.setType(RefactoringType.MOVE_OPERATION);
        // Move Main.foobar -> OtherClass.foobar
        MoveRenameMethodObject foobarObject = new MoveRenameMethodObject("Main.java", "Main",
                foobar, "OtherClass.java", "OtherClass", foobar);
        foobarObject.setType(RefactoringType.MOVE_OPERATION);
        MoveRenameMethodObject moveRenameObject = new MoveRenameMethodObject("OtherClass.java", "OtherClass",
                equals, "Main.java", "Main", isEqual);
        moveRenameObject.setType(RefactoringType.MOVE_OPERATION);
        moveRenameObject.setType(RefactoringType.RENAME_METHOD);

        ReplayMoveRenameMethod replayMoveRenameMethod = new ReplayMoveRenameMethod(project);
        replayMoveRenameMethod.replayMoveRenameMethod(foobarObject);
        replayMoveRenameMethod.replayMoveRenameMethod(fooObject);
        replayMoveRenameMethod.replayMoveRenameMethod(moveRenameObject);


        LightJavaCodeInsightFixtureTestCase.assertEquals(psiFiles[2].getText(), psiFiles[0].getText());
        LightJavaCodeInsightFixtureTestCase.assertEquals(psiFiles[3].getText(), psiFiles[1].getText());
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
        ReplayMoveRenameClass replayMoveRenameClass = new ReplayMoveRenameClass(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        replayMoveRenameClass.replayMoveRenameClass(refactoringObject);

        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);

    }

    public void testReplayMoveClass() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/classRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testFile = "ClassRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile);
        String originalPackage = "renameTestData.classRenameTestData";
        String destinationPackage = "renameTestData";
        Assert.assertNotEquals(destinationPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());
        MoveRenameClassObject moveClass = new MoveRenameClassObject("ClassRenameTestData.java", "ClassRenameTestData", originalPackage,
                "ClassRenameTestData.java", "ClassRenameTestData", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        ReplayMoveRenameClass replayMoveRenameClass = new ReplayMoveRenameClass(project);
        replayMoveRenameClass.replayMoveRenameClass(moveClass);
        Assert.assertEquals(destinationPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());

    }

    public void testReplayMoveOuterToInnerClassBetweenFiles() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        String testFile = "TopClass.java";
        myFixture.configureByFiles(testDataBefore + testFile, testDataAfter + "TopClass.java");
        String originalPackage = "moveRenameClass.after";
        String destinationPackage = "moveRenameClass.before.Class1";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("TopClass.java",
                "moveRenameClass.after.Class2", originalPackage,
                "TopClass.java", "moveRenameClass.before.Class1.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setOuterToInner();
        ReplayMoveRenameClass replay = new ReplayMoveRenameClass(project);
        replay.replayMoveRenameClass(moveClass);
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
        assert psiPackage != null;
        PsiClass[] psiClasses = psiPackage.getClasses();
        Assert.assertEquals(0, psiClasses.length);
        psiPackage = JavaPsiFacade.getInstance(project).findPackage("moveRenameClass.before");
        assert psiPackage != null;
        psiClasses = psiPackage.getClasses();
        Assert.assertEquals(1, psiClasses.length);
        PsiClass[] innerClasses = psiClasses[0].getInnerClasses();
        Assert.assertEquals(1, innerClasses.length);
        Assert.assertEquals("moveRenameClass.before.Class1.Class2", innerClasses[0].getQualifiedName());
    }

    public void testReplayMoveOuterToInnerClassInSameFile() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testData = testDir + "after/";
        String testFile = "InnerClassTest.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testData + testFile);
        String destinationPackage = "moveRenameClass.Class1";
        String originalPackage = "moveRenameClass";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("InnerClassTest.java",
                "moveRenameClass.Class2", originalPackage,
                "InnerClassTest.java", "moveRenameClass.Class1.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setSameFile();
        moveClass.setOuterToInner();
        ReplayMoveRenameClass replay = new ReplayMoveRenameClass(project);
        replay.replayMoveRenameClass(moveClass);
        Assert.assertEquals(originalPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());

    }

    public void testReplayMoveInnerToOuterClassBetweenFiles() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        String testFile = "TopClass.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataBefore + testFile, testDataAfter + "TopClass.java");
        String originalPackage = "moveRenameClass.before.Class1";
        String destinationPackage = "moveRenameClass.after";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("InnerClassTest.java",
                "moveRenameClass.beforeClass1.Class2", originalPackage,
                "Empty.java", "moveRenameClass.after.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setInnerToOuter();
        ReplayMoveRenameClass replay = new ReplayMoveRenameClass(project);
        replay.replayMoveRenameClass(moveClass);
        PsiClass topClass = ((PsiJavaFile) psiFiles[0]).getClasses()[0];
        Assert.assertEquals(0, topClass.getInnerClasses().length);
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
        assert psiPackage != null;
        PsiClass[] psiClasses = psiPackage.getClasses();
        Assert.assertEquals(1, psiClasses.length);
        Assert.assertEquals("moveRenameClass.after.Class2", psiClasses[0].getQualifiedName());
    }

    public void testReplayMoveInnerToOuterClassInSameFile() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testData = testDir + "before/";
        String testFile = "InnerClassTest.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testData + testFile);
        String destinationPackage = "moveRenameClass";
        String originalPackage = "moveRenameClass.Class1";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("InnerClassTest.java",
                "moveRenameClass.Class1.Class2", originalPackage,
                "InnerClassTest.java", "moveRenameClassClass2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setSameFile();
        moveClass.setInnerToOuter();
        ReplayMoveRenameClass replay = new ReplayMoveRenameClass(project);
        replay.replayMoveRenameClass(moveClass);
        Assert.assertEquals(destinationPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());
    }

    public void testReplayMoveInnerToInnerClass() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataAfter + "Class3.java", testDataBefore + "TopClass.java");
        String originalPackage = "moveRenameClass.after.Class3";
        String destinationPackage = "moveRenameClass.Class1";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("Class3.java",
                "moveRenameClass.after.Class3.Class2", originalPackage,
                "TopClass.java", "moveRenameClass.before.Class1.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setInnerToInner();
        ReplayMoveRenameClass replay = new ReplayMoveRenameClass(project);
        replay.replayMoveRenameClass(moveClass);
        PsiClass topClass = ((PsiJavaFile) psiFiles[1]).getClasses()[0];
        Assert.assertEquals(1, topClass.getInnerClasses().length);
        topClass = ((PsiJavaFile) psiFiles[0]).getClasses()[0];
        Assert.assertEquals(0, topClass.getInnerClasses().length);
    }

    public void testReplayMultipleExtractMethodNewLine() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String testDir = "/extractTestData/extractMethod/";
        String resultsTestData = testDir + "expectedReplayResults/";
        String refactoredTestData = testDir + "refactored/";
        String testFile = "Main.java";
        String resultFile = "ReplayResultsWithAddedLine.java";
        PsiFile[] files = myFixture.configureByFiles(refactoredTestData + testFile, resultsTestData + resultFile);
        testDir = basePath + "/" + getTestDataPath() + testDir;
        String originalTestData = testDir + "original/";
        refactoredTestData = testDir + "refactored/";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalTestData, refactoredTestData);
        assert refactorings != null;
        Refactoring firstRef = refactorings.get(0);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(firstRef);
        InvertExtractMethod invertExtractMethod = new InvertExtractMethod(project);
        refactoringObject = invertExtractMethod.invertExtractMethod(refactoringObject);
        Refactoring secondRef = refactorings.get(1);
        RefactoringObject secondRefactoringObject = RefactoringObjectUtils.createRefactoringObject(secondRef);
        secondRefactoringObject = invertExtractMethod.invertExtractMethod(secondRefactoringObject);

        ReplayExtractMethod replayExtractMethod = new ReplayExtractMethod(project);
        try {
            replayExtractMethod.replayExtractMethod(secondRefactoringObject);
            replayExtractMethod.replayExtractMethod(refactoringObject);
        } catch(Exception e) {
            e.printStackTrace();
        }

        PsiFile file1 = files[0];
        PsiFile file2 = files[1];
        String content1 = file1.getText();
        String content2 = file2.getText();
        LightJavaCodeInsightFixtureTestCase.assertEquals(content2, content1);

    }

    public void testReplayInlineMethod() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String testDir = "/extractTestData/extractMethod/";
        String resultsTestData = testDir + "expectedUndoResults/";
        String refactoredTestData = testDir + "refactored/";
        String testFile = "Main.java";
        String resultFile = "Results.java";
        PsiFile[] files = myFixture.configureByFiles(refactoredTestData + testFile, resultsTestData + resultFile);
        testDir = basePath + "/" + getTestDataPath() + testDir;
        String originalTestData = testDir + "refactored/";
        refactoredTestData = testDir + "original/";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("INLINE_OPERATION",
                originalTestData, refactoredTestData);
        assert refactorings != null;
        Refactoring ref = refactorings.get(1);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        ReplayInlineMethod replayOperations = new ReplayInlineMethod(project);
        replayOperations.replayInlineMethod(refactoringObject);
        ref = refactorings.get(0);
        refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        replayOperations.replayInlineMethod(refactoringObject);

        PsiFile file1 = files[0];
        PsiFile file2 = files[1];
        String content1 = file1.getText();
        String content2 = file2.getText();
        LightJavaCodeInsightFixtureTestCase.assertEquals(content1, content2);

    }
}
