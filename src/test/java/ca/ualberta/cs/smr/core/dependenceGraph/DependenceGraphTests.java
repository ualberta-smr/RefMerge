package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import ca.ualberta.cs.smr.utils.sortingUtils.SortPairs;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class DependenceGraphTests {

    @Test
    public void dependenceGraphTest() {
        List<Pair> pairsToSort = new ArrayList<>();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);

        assert classRefs != null;
        pairsToSort.add(new Pair(0, classRefs.get(0))); // A -> newA
        assert methodRefs != null;
        pairsToSort.add(new Pair(0, methodRefs.get(0))); // newA.foo -> newA.bar
        pairsToSort.add(new Pair(1, classRefs.get(1))); // B -> newB
        pairsToSort.add(new Pair(1, methodRefs.get(1))); // newA.add -> newA.addition
        pairsToSort.add(new Pair(2, methodRefs.get(2))); // newB.doBStuff -> newB.doStuff

        for( Pair pair : pairsToSort) {
            System.out.println(pair.getValue().toString());
        }
        SortPairs.sortList(pairsToSort);
        for(Pair pair : pairsToSort) {
            System.out.println(pair.getValue().toString());
        }
        Graph graph = new Graph(pairsToSort);
        System.out.println("GRAPH");
        graph.printGraph();
    }
}
