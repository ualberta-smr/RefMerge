package ca.ualberta.cs.smr.refmerge;

import ca.ualberta.cs.smr.refmerge.invertOperations.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.testUtils.TestUtils;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.*;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.*;


public class UndoOperationsTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }


    public void testUndoRenameMethod() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/methodRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedUndoResults/";
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
        InvertMoveRenameMethod undo = new InvertMoveRenameMethod(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        undo.invertMoveRenameMethod(refactoringObject);

        list1 = TestUtils.getMethodNames(oldMethods);
        list2 = TestUtils.getMethodNames(newMethods);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testUndoMoveRenameMethod() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameMethod/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile = "Main.java";
        String testFile2 = "OtherClass.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataRefactored + testFile2,
                testDataOriginal + testFile, testDataOriginal + testFile2);

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

        InvertMoveRenameMethod undo = new InvertMoveRenameMethod(project);
        undo.invertMoveRenameMethod(fooObject);
        undo.invertMoveRenameMethod(moveRenameObject);
        undo.invertMoveRenameMethod(foobarObject);

        LightJavaCodeInsightFixtureTestCase.assertEquals(psiFiles[0].getText(), psiFiles[2].getText());
        LightJavaCodeInsightFixtureTestCase.assertEquals(psiFiles[1].getText(), psiFiles[3].getText());
    }

    public void testUndoRenameClass() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/classRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedUndoResults/";
        String testFile = "ClassRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile, testResult + testFile);
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
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(refactoringObject);

        list1 = TestUtils.getClassNames(oldClasses);
        list2 = TestUtils.getClassNames(newClasses);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);

    }

    public void testUndoMoveClass() {
        Project project = myFixture.getProject();
        String testDir = "renameTestData/classRenameTestData/";
        String testDataRenamed = testDir + "renamed/";
        String testFile = "ClassRenameTestData.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile);
        String destinationPackage = "renameTestData.classRenameTestData";
        String originalPackage = "renameTestData.original";
        Assert.assertNotEquals(originalPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());
        MoveRenameClassObject moveClass = new MoveRenameClassObject("ClassRenameTestData.java", "ClassRenameTestData", originalPackage,
                "ClassRenameTestData.java", "ClassRenameTestData", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        Assert.assertEquals(originalPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());

    }

    public void testUndoMoveOuterToInnerClassBetweenFiles() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        String testFile = "InnerClassTest.java";
        myFixture.configureByFiles(testDataBefore + testFile, testDataAfter + "Empty.java");
        String originalPackage = "moveRenameClass.after";
        String destinationPackage = "moveRenameClass";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("Empty.java",
                "moveRenameClass.after.Class2", originalPackage,
                "InnerClassTest.java", "moveRenameClass.Class1.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setOuterToInner();
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
        assert psiPackage != null;
        PsiClass[] psiClasses = psiPackage.getClasses();
        Assert.assertEquals(1, psiClasses.length);
        Assert.assertEquals("moveRenameClass.after.Class2", psiClasses[0].getQualifiedName());
        psiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
        assert psiPackage != null;
        psiClasses = psiPackage.getClasses();
        Assert.assertEquals(0, psiClasses.length);
    }

    public void testUndoMoveOuterToInnerClassInSameFile() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testData = testDir + "before/";
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
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        Assert.assertEquals(originalPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());
    }

    public void testUndoMoveInnerToOuterClassBetweenFiles() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        String testFile = "TopClass.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataAfter + testFile, testDataBefore + testFile);
        String originalPackage = "moveRenameClass.before.Class1";
        String destinationPackage = "moveRenameClass.after";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("TopClass.java",
                "moveRenameClass.before.Class1.Class2", originalPackage,
                "TopClass.java", "moveRenameClass.after.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setInnerToOuter();
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        PsiClass topClass = ((PsiJavaFile) psiFiles[1]).getClasses()[0];
        Assert.assertEquals(1, topClass.getInnerClasses().length);
    }

    public void testUndoMoveInnerToOuterClassInSameFile() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testData = testDir + "after/";
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
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        Assert.assertEquals(destinationPackage, ((PsiJavaFile)psiFiles[0]).getPackageName());
    }

    public void testUndoMoveInnerToInnerClass() {
        Project project = myFixture.getProject();
        String testDir = "moveRenameClass/";
        String testDataBefore = testDir + "before/";
        String testDataAfter = testDir + "after/";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataAfter + "Class3.java", testDataBefore + "TopClass.java");
        String originalPackage = "moveRenameClass.Class1";
        String destinationPackage = "moveRenameClass.after.Class3";
        MoveRenameClassObject moveClass = new MoveRenameClassObject("TopClass.java",
                "moveRenameClass.before.Class1.Class2", originalPackage,
                "Class3.java", "moveRenameClass.after.Class3.Class2", destinationPackage);
        moveClass.setType(RefactoringType.MOVE_CLASS);
        moveClass.setInnerToInner();
        InvertMoveRenameClass undo = new InvertMoveRenameClass(project);
        undo.invertMoveRenameClass(moveClass);
        PsiClass topClass = ((PsiJavaFile) psiFiles[1]).getClasses()[0];
        Assert.assertEquals(1, topClass.getInnerClasses().length);
        topClass = ((PsiJavaFile) psiFiles[0]).getClasses()[0];
        Assert.assertEquals(0, topClass.getInnerClasses().length);
    }


    public void testUndoMultipleExtractMethod() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String testDir = "/extractTestData/extractMethod/";
        String resultsTestData = testDir + "expectedUndoResults/";
        String refactoredTestData = testDir + "refactored/";
        String testFile = "Main.java";
        String resultFile = "Results.java";
        PsiFile[] files = myFixture.configureByFiles(refactoredTestData + testFile, resultsTestData + resultFile);
        testDir = basePath + "/" + getTestDataPath() + testDir;
        String originalTestData = testDir + "original/";
        refactoredTestData = testDir + "refactored/";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("EXTRACT_OPERATION",
                originalTestData, refactoredTestData);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        InvertExtractMethod undoOperations = new InvertExtractMethod(project);
        undoOperations.invertExtractMethod(refactoringObject);
        ref = refactorings.get(1);
        refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        undoOperations.invertExtractMethod(refactoringObject);

        PsiFile file1 = files[0];
        PsiFile file2 = files[1];
        String content1 = file1.getText();
        String content2 = file2.getText();
        LightJavaCodeInsightFixtureTestCase.assertEquals(content1, content2);

    }

    public void testUndoInlineMethod() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String testDir = "/extractTestData/extractMethod/";
        String resultsTestData = testDir + "expectedReplayResults/";
        String refactoredTestData = testDir + "original/";
        String testFile = "Main.java";
        String resultFile = "ReplayResults.java";
        PsiFile[] files = myFixture.configureByFiles(refactoredTestData + testFile, resultsTestData + resultFile);
        testDir = basePath + "/" + getTestDataPath() + testDir;
        String originalTestData = testDir + "refactored/";
        refactoredTestData = testDir + "original/";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("INLINE_OPERATION",
                originalTestData, refactoredTestData);
        assert refactorings != null;
        Refactoring ref = refactorings.get(1);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        InvertInlineMethod undoOperations = new InvertInlineMethod(project);
        undoOperations.invertInlineMethod(refactoringObject);
        ref = refactorings.get(0);
        refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        undoOperations.invertInlineMethod(refactoringObject);

        PsiFile file1 = files[0];
        PsiFile file2 = files[1];
        String content1 = file1.getText();
        String content2 = file2.getText();
        LightJavaCodeInsightFixtureTestCase.assertEquals(content1, content2);

    }

    public void testUndoRenameField() {
        Project project = myFixture.getProject();
        String testDir = "renameField/";
        String testDataRenamed = testDir + "renamed/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedUndoResults/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRenamed + testFile, testResult + testFile);
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
        InvertMoveRenameField undo = new InvertMoveRenameField(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        undo.invertRenameField(refactoringObject);

        list1 = TestUtils.getFieldNames(oldFields);
        list2 = TestUtils.getFieldNames(newFields);

        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
        Assert.assertNotEquals(newFields[0].getName(),"renamedField");
        Assert.assertNotEquals(oldFields[0].getName(), "renamedField");
    }

    public void testUndoMoveField() {
        Project project = myFixture.getProject();
        String testDir = "renameMoveFieldFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedAfterInvert/";
        String testFile ="Main.java";
        String testFile2 = "Second.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataRefactored + testFile2,
                testResult + testFile, testResult + testFile2);
        String basePath = System.getProperty("user.dir");

        PsiField[] oldFields = null;
        PsiField[] newFields = null;


        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("expected")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile((file));
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
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
        InvertMoveRenameField undo = new InvertMoveRenameField(project);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        undo.invertRenameField(refactoringObject);

        MoveRenameFieldObject fieldObject = new MoveRenameFieldObject("Main.java", "Main",
                "firstFieldName", "Second.java", "Second", "firstFieldName2");
        fieldObject.setType(RefactoringType.MOVE_RENAME_ATTRIBUTE);
        undo.invertRenameField(fieldObject);

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("expected")) {
                if(file.getName().contains("Main")) {
                    oldFields = TestUtils.getPsiFieldsFromFile((file));
                }
            }
            if(file.getVirtualFile().getCanonicalPath().contains("refactored")) {
                if(file.getName().contains("Main")) {
                    newFields = TestUtils.getPsiFieldsFromFile(file);
                }
            }
        }

        list1 = TestUtils.getFieldNames(oldFields);
        list2 = TestUtils.getFieldNames(newFields);
        LightJavaCodeInsightFixtureTestCase.assertSameElements(list1, list2);
    }

    public void testUndoPullUpMethod() {
        Project project = myFixture.getProject();
        String testDir = "pullUpMethodFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testResult = testDir + "expectedAfterInvert/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testResult + testFile);
        String basePath = System.getProperty("user.dir");

        PsiMethod[] oldMethods;
        PsiMethod[] newMethods;

        List<Pair<String, String>> list1 = new ArrayList<>();
        List<Pair<String, String>> list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("expected")) {
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
        InvertPullUpMethod undo = new InvertPullUpMethod(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PullUpMethodObject refactoringObject2 = (PullUpMethodObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PullUpMethodObject) refactoringObject1).addSubClass(subClasses.get(0));
        undo.invertPullUpMethod(refactoringObject1);

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();

        for(PsiFile file : psiFiles) {
            if(Objects.requireNonNull(file.getVirtualFile().getCanonicalPath()).contains("expected")) {
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

    public void testUndoPushDownMethod() {
        Project project = myFixture.getProject();
        String testDir = "pullUpMethodFiles/";
        String testDataOriginal = testDir + "refactored/";
        String testDataRefactored = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataOriginal + testFile);
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
        InvertPushDownMethod undo = new InvertPushDownMethod(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PushDownMethodObject refactoringObject2 = (PushDownMethodObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PushDownMethodObject) refactoringObject1).addSubClass(subClasses.get(0));
        undo.invertPushDownMethod(refactoringObject1);

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


    public void testUndoPullUpField() {
        Project project = myFixture.getProject();
        String testDir = "pullUpFieldFiles/";
        String testDataRefactored = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataOriginal + testFile);
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
        InvertPullUpField undo = new InvertPullUpField(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PullUpFieldObject refactoringObject2 = (PullUpFieldObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PullUpFieldObject) refactoringObject1).addSubClass(subClasses.get(0));
        undo.invertPullUpField(refactoringObject1);

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

    public void testInvertPushDownField() {
        Project project = myFixture.getProject();
        String testDir = "pullUpFieldFiles/";
        String testDataOriginal = testDir + "refactored/";
        String testDataRefactored = testDir + "original/";
        String testFile ="Main.java";
        PsiFile[] psiFiles = myFixture.configureByFiles(testDataRefactored + testFile, testDataOriginal + testFile);
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
        InvertPushDownField invert = new InvertPushDownField(project);
        RefactoringObject refactoringObject1 = RefactoringObjectUtils.createRefactoringObject(ref);
        // Get the second subclass to add to first ref object
        PushDownFieldObject refactoringObject2 = (PushDownFieldObject) RefactoringObjectUtils.createRefactoringObject(ref2);
        assert refactoringObject2 != null;
        List<Pair<String, String>> subClasses = refactoringObject2.getSubClasses();
        assert refactoringObject1 != null;
        ((PushDownFieldObject) refactoringObject1).addSubClass(subClasses.get(0));
        invert.invertPushDownField(refactoringObject1);

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


    public void testInvertRenamePackage() {
        Project project = myFixture.getProject();
        String testDir = "renamePackageFiles/";
        String testDataOriginal = testDir + "refactored/";
        String testFile ="Main.java";
        myFixture.configureByFiles(testDataOriginal + testFile);

        RenamePackageObject refactoringObject = new RenamePackageObject("renamePackageFiles.expectedPackageName", "renamePackageFiles.refactored");
        String destinationPackage = refactoringObject.getDestinationName();

        PsiPackage destinationPsiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
        assert destinationPsiPackage != null;

        String psiPackageName = destinationPsiPackage.getName();
        Assert.assertNotEquals(psiPackageName, "expectedPackageName");

        InvertRenamePackage invert = new InvertRenamePackage(project);
        invert.invertRenamePackage(refactoringObject);


        destinationPsiPackage = JavaPsiFacade.getInstance(project).findPackage(destinationPackage);
        assert destinationPsiPackage == null;

        String originalPackage = refactoringObject.getOriginalName();
        PsiPackage originalPsiPackage = JavaPsiFacade.getInstance(project).findPackage(originalPackage);
        assert originalPsiPackage != null;
        psiPackageName = originalPsiPackage.getName();
        Assert.assertEquals(psiPackageName, "expectedPackageName");
    }


}
