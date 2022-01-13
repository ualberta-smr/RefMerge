package ca.ualberta.cs.smr.refmerge.matrix;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.RefactoringDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.receivers.Receiver;
import ca.ualberta.cs.smr.refmerge.matrix.receivers.MoveRenameClassReceiver;
import ca.ualberta.cs.smr.refmerge.matrix.receivers.MoveRenameMethodReceiver;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class MatrixTest extends LightJavaCodeInsightFixtureTestCase {

    public void testElementMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        MoveRenameClassDispatcher renameClassElement = new MoveRenameClassDispatcher();
        MoveRenameMethodDispatcher renameMethodElement = new MoveRenameMethodDispatcher();
        RefactoringDispatcher element = Matrix.dispatcherMap.get(type);
        boolean equals = element.getClass().equals(renameClassElement.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        element = Matrix.dispatcherMap.get(type);
        equals = element.getClass().equals(renameMethodElement.getClass());
        Assert.assertTrue(equals);
    }

    public void testReceiverMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        MoveRenameClassReceiver moveRenameClassReceiver = new MoveRenameClassReceiver();
        MoveRenameMethodReceiver moveRenameMethodReceiver = new MoveRenameMethodReceiver();
        Receiver receiver = Matrix.receiverMap.get(type);
        boolean equals = receiver.getClass().equals(moveRenameClassReceiver.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        receiver = Matrix.receiverMap.get(type);
        equals = receiver.getClass().equals(moveRenameMethodReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<RefactoringObject> refactorings = GetDataForTests.getRefactoringObjects("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        RefactoringObject refactoringObject = refactorings.get(0);
        MoveRenameMethodDispatcher mockElement = new MoveRenameMethodDispatcher();
        Matrix matrix = new Matrix(null);
        assert refactoringObject != null;
        RefactoringDispatcher element = matrix.makeDispatcher(refactoringObject, false);
        boolean equals = element.getClass().equals(mockElement.getClass());
        Assert.assertTrue(equals);


    }

    public void testMakeReceiver() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        MoveRenameMethodReceiver mockReceiver = new MoveRenameMethodReceiver();
        Matrix matrix = new Matrix(null);
        assert refactoringObject != null;
        Receiver receiver = matrix.makeReceiver(refactoringObject);
        boolean equals = receiver.getClass().equals(mockReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testGetRefactoringValue() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String renamedPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<RefactoringObject> refactorings = GetDataForTests.getRefactoringObjects("RENAME_METHOD", originalPath, renamedPath);
        assert refactorings != null;
        RefactoringObject renameMethod = refactorings.get(1);
        originalPath = basePath + "/src/test/resources/extractTestData/extractMethod/original/";
        String extractedPath = basePath + "/src/test/resources/extractTestData/extractMethod/refactored/";
        refactorings = GetDataForTests.getRefactoringObjects("EXTRACT_OPERATION", originalPath, extractedPath);
        assert refactorings != null;
        RefactoringObject extractMethod = refactorings.get(0);
        Matrix matrix = new Matrix(project);
        assert extractMethod != null && renameMethod != null;
        int renameValue = matrix.getRefactoringValue(renameMethod.getRefactoringType());
        int extractValue = matrix.getRefactoringValue(extractMethod.getRefactoringType());
        Assert.assertTrue(renameValue < extractValue);

    }

    public void testSimplifyAndInsertRefactorings() {
        List<ParameterObject> parameters = new ArrayList<>();
        parameters.add(new ParameterObject("int", "return"));
        parameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(parameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(parameters, "bar");
        MethodSignatureObject foobar = new MethodSignatureObject(parameters, "foobar");
        MethodSignatureObject extractedMethod = new MethodSignatureObject(parameters, "extractedMethod");
        MethodSignatureObject m1 = new MethodSignatureObject(parameters, "m1");
        MethodSignatureObject m2 = new MethodSignatureObject(parameters, "m2");
        MethodSignatureObject newName = new MethodSignatureObject(parameters, "newName");
        // (1) A.foo -> A.bar
        MoveRenameMethodObject refactoring1 = new MoveRenameMethodObject("A.java", "A", foo,
                "A.java", "A", bar);
        // (2) A -> B
        MoveRenameClassObject refactoring2 = new MoveRenameClassObject("A.java", "A", "package",
                "B.java", "B", "package");
        // (3) B -> C
        MoveRenameClassObject refactoring3 = new MoveRenameClassObject("B.java", "B", "package",
                "C.java", "C", "package");
        // (3) B.bar -> C.foobar
        MoveRenameMethodObject refactoring4 = new MoveRenameMethodObject("B.java", "B", bar,
                "C.java", "C", foobar);
        // (4) C.extractedMethod from C.foobar
        ExtractMethodObject refactoring5 = new ExtractMethodObject("C.java", "C", foobar,
                "C.java", "C", extractedMethod);
        // (5) X.m1 -> X.m2
        MoveRenameMethodObject refactoring6 = new MoveRenameMethodObject("X.java", "X", m1,
                "X.java", "X", m2);
        // (6) C -> D
        MoveRenameClassObject refactoring7 = new MoveRenameClassObject("C.java", "C", "package",
                "D.java", "D", "package");
        // (7) D.extractedMethod -> D.newName
        MoveRenameMethodObject refactoring8 = new MoveRenameMethodObject("D.java", "D", extractedMethod,
                "D.java", "D", newName);


        ArrayList<RefactoringObject> simplifiedRefactorings = new ArrayList<>();
        Matrix matrix = new Matrix(null);
        matrix.simplifyAndInsertRefactorings(refactoring1, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring2, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring3, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring4, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring5, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring6, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring7, simplifiedRefactorings);
        matrix.simplifyAndInsertRefactorings(refactoring8, simplifiedRefactorings);

        // A -> D
        MoveRenameClassObject expected1 = new MoveRenameClassObject("A.java", "A", "package",
                "D.java", "D", "package");
        // X.m1 -> X.m2
        MoveRenameMethodObject expected2 = new MoveRenameMethodObject("X.java", "X", m1,
                "X.java", "X", m2);
        // A.foo -> D.foobar
        MoveRenameMethodObject expected3 = new MoveRenameMethodObject("A.java", "A", foo,
                "D.java", "D", foobar);
        // D.newName extracted from A.foo
        ExtractMethodObject expected4 = new ExtractMethodObject("A.java", "A", foo,
                "D.java", "D", newName);

        ArrayList<RefactoringObject> expectedRefactorings = new ArrayList<>();
        expectedRefactorings.add(expected1);
        expectedRefactorings.add(expected2);
        expectedRefactorings.add(expected3);
        expectedRefactorings.add(expected4);

        Assert.assertEquals(expectedRefactorings.size(), simplifiedRefactorings.size());
        for(int i = 0; i < expectedRefactorings.size(); i++) {
            RefactoringObject simplifiedRefactoring = simplifiedRefactorings.get(i);
            RefactoringObject expectedRefactoring = expectedRefactorings.get(i);
            switch(expectedRefactoring.getRefactoringType()) {
                case RENAME_CLASS:
                    compareRenameClass(expectedRefactoring, simplifiedRefactoring);
                    break;
                case RENAME_METHOD:
                    compareRenameMethod(expectedRefactoring, simplifiedRefactoring);
                    break;
                case EXTRACT_OPERATION:
                    compareExtractMethod(expectedRefactoring, simplifiedRefactoring);
            }
        }
    }

    public void testRunMatrix() {
        Project project = myFixture.getProject();
        List<ParameterObject> parameters = new ArrayList<>();
        parameters.add(new ParameterObject("int", "return"));
        parameters.add(new ParameterObject("int", "x"));
        MethodSignatureObject foo = new MethodSignatureObject(parameters, "foo");
        MethodSignatureObject bar = new MethodSignatureObject(parameters, "bar");
        // (1) A.foo -> A.bar
        MoveRenameMethodObject leftMethodObject1 = new MoveRenameMethodObject("A.java", "A", foo,
                "A.java", "A", bar);
        leftMethodObject1.setType(RefactoringType.RENAME_METHOD);
        // (1) A.foo -> B.foo
        MoveRenameMethodObject rightMethodObject1 = new MoveRenameMethodObject("A.java", "A", foo,
                "B.java", "B", foo);
        rightMethodObject1.setType(RefactoringType.MOVE_OPERATION);

        ArrayList<RefactoringObject> leftRefactoringList = new ArrayList<>();
        ArrayList<RefactoringObject> rightRefactoringList = new ArrayList<>();

        leftRefactoringList.add(leftMethodObject1);
        rightRefactoringList.add(rightMethodObject1);

        Matrix matrix = new Matrix(project);

        Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, ArrayList<RefactoringObject>>
                resultingList = matrix.detectConflicts(leftRefactoringList, rightRefactoringList);
        RefactoringObject actualObject = resultingList.getRight().get(0);

        MethodSignatureObject actualSignature = ((MoveRenameMethodObject) actualObject).getDestinationMethodSignature();
        String actualClass = ((MoveRenameMethodObject) actualObject).getDestinationClassName();
        Assert.assertEquals(actualSignature, bar);
        Assert.assertEquals("B", actualClass);

    }

    private void compareRenameClass(RefactoringObject expected, RefactoringObject simplified) {
        Assert.assertEquals(expected.getDestinationFilePath(), simplified.getDestinationFilePath());
        Assert.assertEquals(((MoveRenameClassObject) simplified).getDestinationClassObject(),
                ((MoveRenameClassObject) simplified).getDestinationClassObject());
        Assert.assertEquals(expected.getOriginalFilePath(), simplified.getOriginalFilePath());
        Assert.assertEquals(((MoveRenameClassObject) expected).getOriginalClassObject().getClassName(),
                ((MoveRenameClassObject) simplified).getOriginalClassObject().getClassName());
    }

    private void compareRenameMethod(RefactoringObject expected, RefactoringObject simplified) {
        MethodSignatureObject firstOriginalSignature = ((MoveRenameMethodObject) simplified).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((MoveRenameMethodObject) expected).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((MoveRenameMethodObject) simplified).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((MoveRenameMethodObject) expected).getDestinationMethodSignature();
        Assert.assertEquals(expected.getDestinationFilePath(), simplified.getDestinationFilePath());
        Assert.assertEquals(((MoveRenameMethodObject) expected).getDestinationClassName(),
                ((MoveRenameMethodObject) simplified).getDestinationClassName());
        Assert.assertTrue(firstOriginalSignature.equalsSignature(expectedOriginalSignature));
        Assert.assertTrue(firstDestinationSignature.equalsSignature(expectedDestinationSignature));
    }

    private void compareExtractMethod(RefactoringObject expected, RefactoringObject simplified) {
        MethodSignatureObject firstOriginalSignature = ((ExtractMethodObject) simplified).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((ExtractMethodObject) expected).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((ExtractMethodObject) simplified).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((ExtractMethodObject) expected).getDestinationMethodSignature();

        Assert.assertEquals(expected.getOriginalFilePath(), simplified.getOriginalFilePath());
        Assert.assertEquals(expected.getDestinationFilePath(), simplified.getDestinationFilePath());
        Assert.assertEquals(((ExtractMethodObject) expected).getOriginalClassName(),
                ((ExtractMethodObject) simplified).getOriginalClassName());
        Assert.assertEquals(((ExtractMethodObject) expected).getDestinationClassName(),
                ((ExtractMethodObject) simplified).getDestinationClassName());
        Assert.assertTrue(expectedOriginalSignature.equalsSignature(firstOriginalSignature));
        Assert.assertTrue(expectedDestinationSignature.equalsSignature(firstDestinationSignature));
    }
}
