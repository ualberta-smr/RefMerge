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
        leftList.add(new Pair(0, classRefs.get(1))); // Original -> A
        leftList.add(new Pair(1, methodRefs.get(2))); // A.foo -> A.bar
        rightList.add(new Pair(0, methodRefs.get(3))); //Original.foo -> Original.fuzz

        List<Refactoring> expectedList = new ArrayList<>();
        expectedList.add(methodRefs.get(3));
        expectedList.add(classRefs.get(1));
        expectedList.add(methodRefs.get(2));

        SortPairs.sortList(leftList);
        SortPairs.sortList(rightList);

        DependenceGraph graph = new DependenceGraph(project);
        graph.createPartialGraph(leftList);
        graph.createPartialGraph(rightList);
        List<Node> originalNodes = graph.getSortedNodes();
        Matrix matrix = new Matrix(project);
        DependenceGraph newGraph = matrix.runMatrix(leftList, rightList);
        System.out.println("\n\nNEW GRAPH:");
        List<Node> sortedNodes = newGraph.getSortedNodes();

        List<Refactoring> unexpectedList = new ArrayList<>();
        List<Refactoring> actualList = new ArrayList<>();
        for(int i = 0; i < originalNodes.size(); i++) {
            unexpectedList.add(originalNodes.get(i).getRefactoring());
            actualList.add(sortedNodes.get(i).getRefactoring());
        }
        Assert.assertNotEquals(unexpectedList, actualList);
        Assert.assertEquals(expectedList, actualList);
    }

}
