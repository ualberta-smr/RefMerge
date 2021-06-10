package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.*;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RefactoringDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameClassReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class MatrixTest extends LightJavaCodeInsightFixtureTestCase {

    public void testElementMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassDispatcher renameClassElement = new RenameClassDispatcher();
        RenameMethodDispatcher renameMethodElement = new RenameMethodDispatcher();
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
        RenameClassReceiver renameClassReceiver = new RenameClassReceiver();
        RenameMethodReceiver renameMethodReceiver = new RenameMethodReceiver();
        Receiver receiver = Matrix.receiverMap.get(type);
        boolean equals = receiver.getClass().equals(renameClassReceiver.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        receiver = Matrix.receiverMap.get(type);
        equals = receiver.getClass().equals(renameMethodReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0).getValue();
        Node node = new Node(ref);
        RenameMethodDispatcher mockElement = new RenameMethodDispatcher();
        Matrix matrix = new Matrix(null);
        RefactoringDispatcher element = matrix.makeDispatcher(node);
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
        Node node = new Node(ref);
        RenameMethodReceiver mockReceiver = new RenameMethodReceiver();
        Matrix matrix = new Matrix(null);
        Receiver receiver = matrix.makeReceiver(node);
        boolean equals = receiver.getClass().equals(mockReceiver.getClass());
        Assert.assertTrue(equals);
    }

    public void testGetRefactoringValue() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String renamedPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, renamedPath);
        assert refactorings != null;
        Refactoring renameMethod = refactorings.get(1).getValue();
        originalPath = basePath + "/src/test/resources/extractTestData/extractMethod/original/";
        String extractedPath = basePath + "/src/test/resources/extractTestData/extractMethod/refactored/";
        refactorings = GetDataForTests.getPairs("EXTRACT_OPERATION", originalPath, extractedPath);
        assert refactorings != null;
        Refactoring extractMethod = refactorings.get(0).getValue();
        Node renameNode = new Node(renameMethod);
        Node extractNode = new Node(extractMethod);
        DependenceGraph graph = new DependenceGraph(project);
        graph.addVertex(renameNode);
        graph.addVertex(extractNode);
        Matrix matrix = new Matrix(project, graph);
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
        RenameMethodObject refactoring1 = new RenameMethodObject("A.java", "A", foo,
                "A.java", "A", bar);
        // (2) A -> B
        RenameClassObject refactoring2 = new RenameClassObject("A.java", "A",
                "B.java", "B");
        // (3) B -> C
        RenameClassObject refactoring3 = new RenameClassObject("B.java", "B",
                "C.java", "C");
        // (3) B.bar -> C.foobar
        RenameMethodObject refactoring4 = new RenameMethodObject("B.java", "B", bar,
                "C.java", "C", foobar);
        // (4) C.extractedMethod from C.foobar
        ExtractMethodObject refactoring5 = new ExtractMethodObject("C.java", "C", foobar,
                "C.java", "C", extractedMethod);
        // (5) X.m1 -> X.m2
        RenameMethodObject refactoring6 = new RenameMethodObject("X.java", "X", m1,
                "X.java", "X", m2);
        // (6) C -> D
        RenameClassObject refactoring7 = new RenameClassObject("C.java", "C",
                "D.java", "D");
        // (7) D.extractedMethod -> D.newName
        RenameMethodObject refactoring8 = new RenameMethodObject("D.java", "D", extractedMethod,
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
        RenameClassObject expected1 = new RenameClassObject("A.java", "A",
                "D.java", "D");
        // X.m1 -> X.m2
        RenameMethodObject expected2 = new RenameMethodObject("X.java", "X", m1,
                "X.java", "X", m2);
        // A.foo -> D.foobar
        RenameMethodObject expected3 = new RenameMethodObject("A.java", "A", foo,
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

    private void compareRenameClass(RefactoringObject expected, RefactoringObject simplified) {
        Assert.assertEquals(expected.getDestinationFilePath(), simplified.getDestinationFilePath());
        Assert.assertEquals(((RenameClassObject) simplified).getDestinationClassName(),
                ((RenameClassObject) simplified).getDestinationClassName());
        Assert.assertEquals(expected.getOriginalFilePath(), simplified.getOriginalFilePath());
        Assert.assertEquals(((RenameClassObject) expected).getOriginalClassName(),
                ((RenameClassObject) simplified).getOriginalClassName());
    }

    private void compareRenameMethod(RefactoringObject expected, RefactoringObject simplified) {
        MethodSignatureObject firstOriginalSignature = ((RenameMethodObject) simplified).getOriginalMethodSignature();
        MethodSignatureObject expectedOriginalSignature = ((RenameMethodObject) expected).getOriginalMethodSignature();
        MethodSignatureObject firstDestinationSignature = ((RenameMethodObject) simplified).getDestinationMethodSignature();
        MethodSignatureObject expectedDestinationSignature = ((RenameMethodObject) expected).getDestinationMethodSignature();
        Assert.assertEquals(expected.getDestinationFilePath(), simplified.getDestinationFilePath());
        Assert.assertEquals(((RenameMethodObject) expected).getDestinationClassName(),
                ((RenameMethodObject) simplified).getDestinationClassName());
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
