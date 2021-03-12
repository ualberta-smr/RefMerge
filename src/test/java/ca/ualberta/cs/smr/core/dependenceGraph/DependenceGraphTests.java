package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import ca.ualberta.cs.smr.utils.sortingUtils.SortPairs;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class DependenceGraphTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testHasDependence() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependenceGraph/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependenceGraph/refactored";

        Project project = myFixture.getProject();
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);

        assert classRefs != null;
        assert methodRefs != null;
        Graph graph = new Graph(project);
        Node methodNode = new Node(methodRefs.get(0));
        Node classNode = new Node(classRefs.get(0));
        boolean hasDependence = graph.hasDependence(methodNode, classNode);
        Assert.assertTrue(hasDependence);
        Node methodNode2 = new Node(methodRefs.get(1));
        hasDependence = graph.hasDependence(methodNode2, classNode);
        Assert.assertTrue(hasDependence);
        hasDependence = graph.hasDependence(methodNode, methodNode2);
        Assert.assertFalse(hasDependence);


    }


    public void testDependenceGraph() {
        String testDir = "renameMethodRenameClassFiles/dependenceGraph/";
        String testDataRenamed = testDir + "refactored/";
        String testDataOriginal = testDir + "original/";
        String testFile = "A.java";
        String testFile2 = "Main.java";
        String testFile3 = "Original.java";
        myFixture.configureByFiles(testDataOriginal + testFile2, testDataRenamed + testFile2,
                testDataOriginal + testFile, testDataRenamed + testFile, testDataOriginal + testFile3, testDataRenamed + testFile3);


        Project project = myFixture.getProject();
        List<Pair> leftList = new ArrayList<>();
        List<Pair> rightList = new ArrayList<>();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependenceGraph/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependenceGraph/refactored";

        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);

        assert classRefs != null;
        assert methodRefs != null;
        leftList.add(new Pair(0, classRefs.get(0))); // Original -> A
        leftList.add(new Pair(1, methodRefs.get(0))); // A.foo -> A.bar
        rightList.add(new Pair(0, methodRefs.get(1))); //Original.foo -> Original.fuzz

        System.out.println("Left List:");
        SortPairs.sortList(leftList);
        for( Pair pair : leftList) {
            System.out.println(pair.getValue().toString());
        }
        System.out.println("Right List");
        SortPairs.sortList(rightList);
        for(Pair pair : rightList) {
            System.out.println(pair.getValue().toString());
        }
        Graph graph = new Graph(project);
        graph.createPartialGraph(leftList);
        graph.createPartialGraph(rightList);
        System.out.println("\n\nGRAPH");
        graph.printGraph();
        System.out.println("\n\n");
        Matrix matrix = new Matrix(project);
        Graph newGraph = matrix.runMatrix(leftList, rightList);
        System.out.println("\n\nNEW GRAPH:");
        List<Node> nodes = newGraph.getSortedNodes();
        for(Node node : nodes) {
            if(node.wasVisited()) {
                System.out.println("VISITED: " + node.getRefactoring().toString());
            }
            else {
                System.out.println("NOT VISITED");
            }
        }
    }
}
