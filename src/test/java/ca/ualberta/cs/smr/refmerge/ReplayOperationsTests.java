package ca.ualberta.cs.smr.refmerge;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.refmerge.replayOperations.*;
import ca.ualberta.cs.smr.refmerge.invertOperations.InvertExtractMethod;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public void testReplayRenameField() {
        Project project = myFixture.getProject();
        String testDir = "renameField/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedReplayResults/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");
        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRenamed;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;

        PsiField[] oldFields = TestUtils.getPsiFieldsFromFile(psiFiles[0]);
        PsiField[] newFields = TestUtils.getPsiFieldsFromFile(psiFiles[1]);

        List<String> list1 = TestUtils.getFieldNames(oldFields);
        List<String> list2 = TestUtils.getFieldNames(newFields);

        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);


        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_ATTRIBUTE", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        ReplayMoveRenameField replay = new ReplayMoveRenameField(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        replay.replayRenameField(refactoringObject);

        list1 = TestUtils.getFieldNames(oldFields);
        list2 = TestUtils.getFieldNames(newFields);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
        Assert.assertNotEquals(newFields[0].getName(),"originalField");
        Assert.assertNotEquals(oldFields[0].getName(), "originalField");
    }

    public void testReplayMoveField() {
        Project project = myFixture.getProject();
        String testDir = "renameMoveFieldFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile ="Main.java";
        String testFile2 = "Second.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testDataOriginal + testFile2,
                testDataRefactored + testFile, testDataRefactored + testFile2);
        String basePath = System.getProperty("user.dir");

        PsiField[] oldFields = null;
        PsiField[] newFields = null;


        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Second")) {
                    oldFields = TestUtils.getPsiFieldsFromFile((file));
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Second")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                }
            }
        }

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;


        assert oldFields != null;
        List<String> list1 = TestUtils.getFieldNames(oldFields);
        assert newFields != null;
        List<String> list2 = TestUtils.getFieldNames(newFields);

        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("MOVE_ATTRIBUTE", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        ReplayMoveRenameField replay = new ReplayMoveRenameField(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        replay.replayRenameField(refactoringObject);

        MoveRenameFieldObject fieldObject = new MoveRenameFieldObject("Main.java", "Main",
                "firstFieldName", "Second.java", "Second", "firstFieldName2");
        fieldObject.setType(RefactoringType.MOVE_RENAME_ATTRIBUTE);
        replay.replayRenameField(fieldObject);

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Second")) {
                    oldFields = TestUtils.getPsiFieldsFromFile((file));
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Second")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                }
            }
        }

        list1 = TestUtils.getFieldNames(oldFields);
        list2 = TestUtils.getFieldNames(newFields);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayPullUpMethod() {
        Project project = myFixture.getProject();
        String testDir = "pullUpMethodFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal+ testFile, testDataRefactored + testFile);
        String basePath = System.getProperty("user.dir");

        PsiMethod[] oldMethods;
        PsiMethod[] newMethods;

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldMethods = TestUtils.getPsiMethodsFromFile((file));
                    for(PsiMethod method : oldMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newMethods = TestUtils.getPsiMethodsFromFile(file);
                    for(PsiMethod method : newMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;




        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("PULL_UP_OPERATION", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Refactoring ref2 = refactorings.get(1);
        ReplayPullUpMethod replay = new ReplayPullUpMethod(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PullUpMethodObject refactoringObject2 = (PullUpMethodObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PullUpMethodObject) refactoringObject1).addSubClass(subClasses.get(0));
        replay.replayPullUpMethod(refactoringObject1);

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldMethods = TestUtils.getPsiMethodsFromFile((file));
                    for(PsiMethod method : oldMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newMethods = TestUtils.getPsiMethodsFromFile(file);
                    for(PsiMethod method : newMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayPushDownMethod() {
        Project project = myFixture.getProject();
        String testDir = "pullUpMethodFiles/";
        String testDataOriginal = testDir + "refactored/";
        String testDataRefactored = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testDataRefactored + testFile);
        String basePath = System.getProperty("user.dir");

        PsiMethod[] oldMethods;
        PsiMethod[] newMethods;

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldMethods = TestUtils.getPsiMethodsFromFile((file));
                    for(PsiMethod method : oldMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newMethods = TestUtils.getPsiMethodsFromFile(file);
                    for(PsiMethod method : newMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;




        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("PUSH_DOWN_OPERATION", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Refactoring ref2 = refactorings.get(1);
        ReplayPushDownMethod replay = new ReplayPushDownMethod(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PushDownMethodObject refactoringObject2 = (PushDownMethodObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PushDownMethodObject) refactoringObject1).addSubClass(subClasses.get(0));
        replay.replayPushDownMethod(refactoringObject1);

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldMethods = TestUtils.getPsiMethodsFromFile((file));
                    for(PsiMethod method : oldMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newMethods = TestUtils.getPsiMethodsFromFile(file);
                    for(PsiMethod method : newMethods) {
                        String methodName = method.getName();
                        String className = Objects.requireNonNull(method.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayPullUpField() {
        Project project = myFixture.getProject();
        String testDir = "pullUpFieldFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testDataRefactored + testFile);
        String basePath = System.getProperty("user.dir");

        PsiField[] oldFields;
        PsiField[] newFields;

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : oldFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : newFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;



        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("PULL_UP_ATTRIBUTE", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Refactoring ref2 = refactorings.get(1);
        ReplayPullUpField replay = new ReplayPullUpField(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PullUpFieldObject refactoringObject2 = (PullUpFieldObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PullUpFieldObject) refactoringObject1).addSubClass(subClasses.get(0));
        replay.replayPullUpField(refactoringObject1);

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : oldFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : newFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayPushDownField() {
        Project project = myFixture.getProject();
        String testDir = "pullUpFieldFiles/";
        String testDataOriginal = testDir + "refactored/";
        String testDataRefactored = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataOriginal + testFile, testDataRefactored + testFile);
        String basePath = System.getProperty("user.dir");

        PsiField[] oldFields;
        PsiField[] newFields;

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : oldFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : newFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;



        LightJavaCodeInsightFixtureTestCase.assertNotSame(list1, list2);

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("PUSH_DOWN_ATTRIBUTE", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Refactoring ref2 = refactorings.get(1);
        ReplayPushDownField replay = new ReplayPushDownField(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PushDownFieldObject refactoringObject2 = (PushDownFieldObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PushDownFieldObject) refactoringObject1).addSubClass(subClasses.get(0));
        replay.replayPushDownField(refactoringObject1);

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : oldFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list1.add(new Pair<>(className, methodName));
                    }
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                    for(PsiField field : newFields) {
                        String methodName = field.getName();
                        String className = Objects.requireNonNull(field.getContainingClass()).getName();
                        list2.add(new Pair<>(className, methodName));
                    }
                }
            }
        }

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testReplayRenamePackage() {
        Project project = myFixture.getProject();
        String testDir = "renamePackageFiles/";
        String testDataOriginal = testDir + "original/";
        String testFile ="Main.java";
        myFixture.configureByFiles(testDataOriginal + testFile);

        RenamePackageObject refactoringObject =
                new RenamePackageObject("renamePackageFiles.original", "renamePackageFiles.expectedPackageName");
        String originalPackage = refactoringObject.getOriginalName();

        PsiPackage originalPsiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
        assert originalPsiPackage != null;

        String psiPackageName = originalPsiPackage.getName();
        Assert.assertNotEquals(psiPackageName, "expectedPackageName");

        ReplayRenamePackage replay = new ReplayRenamePackage(project);
        replay.replayRenamePackage(refactoringObject);


        originalPsiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
        assert originalPsiPackage == null;

        String destinationPackage = refactoringObject.getDestinationName();
        PsiPackage destinationPsiPackage= JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
        assert destinationPsiPackage != null;
        psiPackageName = destinationPsiPackage.getName();
        Assert.assertEquals(psiPackageName, "expectedPackageName");
    }

    public void testInvertRenameParameter() {
        Project project = myFixture.getProject();
        String testDir = "parameterFiles/renameFiles/";
        String testDataOriginal = testDir + "original/";
        String testDataRefactored = testDir + "expected/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataOriginal + testFile);
        String basePath = System.getProperty("user.dir");

        String refactoredPath = basePath + "/" + getTestDataPath() + "/" + testDataRefactored;
        String originalPath = basePath + "/" + getTestDataPath() + "/" + testDataOriginal;

        PsiParameterList oldList;
        PsiParameterList newList;

        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_PARAMETER", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject object = new RenameParameterObject(ref);
        ReplayRenameParameter replay = new ReplayRenameParameter(project);
        replay.replayRenameParameter(object);

        oldList = null;
        newList = null;

        for(PsiFile file : psiFiles) {
            if (Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("original")) {
                PsiMethod[] methods = TestUtils.getPsiMethodsFromFile((file));
                for (PsiMethod method : methods) {
                    if (method.getName().equals("addNumbers")) {
                        oldList = method.getParameterList();
                    }
                }

            }
            if (file.getVirtualFile().getCanonicalPath().contains("expected")) {
                PsiMethod[] methods = TestUtils.getPsiMethodsFromFile((file));
                for (PsiMethod method : methods) {
                    if (method.getName().equals("addNumbers")) {
                        newList = method.getParameterList();
                    }
                }
            }
        }
        assert oldList != null;
        PsiParameter[] oldParameters = oldList.getParameters();
        assert newList != null;
        PsiParameter[] newParameters = newList.getParameters();
        for(int i = 0; i < oldParameters.length; i++) {
            PsiParameter oldParameter = oldParameters[i];
            PsiParameter newParameter = newParameters[i];
            String oldName = oldParameter.getName();
            String newName = newParameter.getName();
            Assert.assertEquals(oldName, newName);
        }

    }

}
