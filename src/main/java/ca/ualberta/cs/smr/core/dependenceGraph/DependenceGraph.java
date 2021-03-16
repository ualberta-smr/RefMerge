package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class DependenceGraph {
    Project project;
    private DefaultDirectedGraph<Node, DefaultEdge> graph;
    private DefaultDirectedGraph<Node, DefaultEdge> tempGraph;

    public DependenceGraph(Project project) {
        this.project = project;
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.tempGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    public DefaultDirectedGraph<Node, DefaultEdge> createPartialGraph(List<Pair> pairs) {
        if(pairs.size() == 0) {
            return null;
        }
        this.tempGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        if(pairs.size() == 1) {
            Node node = new Node(pairs.get(0).getValue());
            tempGraph.addVertex(node);
            Graphs.addGraph(graph, tempGraph);
            return tempGraph;
        }
        for(int i = pairs.size() - 1; i > -1; i--) {
            Pair pair = pairs.get(i);
            Refactoring refactoring = pair.getValue();
            Node node = new Node(refactoring);
            insertVertex(node);
        }
        Graphs.addGraph(graph, tempGraph);
        return tempGraph;
    }

    public void insertVertex(Node node) {
        this.tempGraph.addVertex(node);
        Node temp = null;
        DepthFirstIterator<Node, DefaultEdge> dFI = new DepthFirstIterator<>(graph);
        while(dFI.hasNext()) {
            Node nodeInGraph = dFI.next();
            if(hasDependence(node, nodeInGraph)) {
                node.updateHead(node);
                temp = node;
            }
            if(temp != null) {
                this.tempGraph.addEdge(temp, node);
            }
        }
    }

    public List<Node> getSortedNodes() {
        List<Node> nodes = new ArrayList<>();
        DepthFirstIterator<Node, DefaultEdge> iterator = new DepthFirstIterator<>(this.graph);
        while(iterator.hasNext()) {
            nodes.add(iterator.next());
        }
        return nodes;
    }

    public boolean hasDependence(Node node1, Node node2) {

//        Matrix matrix = new Matrix(project);
//        RefactoringElement element = matrix.makeElement(node1);
//        RefactoringVisitor visitor = matrix.makeVisitor(node2);
//        element.accept(visitor);
        Refactoring refactoring1 = node1.getRefactoring();
        Refactoring refactoring2 = node2.getRefactoring();
        RefactoringType type1 = refactoring1.getRefactoringType();
        RefactoringType type2 = refactoring2.getRefactoringType();
        if(type1 == RefactoringType.RENAME_CLASS && type2 == RefactoringType.RENAME_CLASS) {
            // Only need to check this case because the node cannot depend on the new node
            String class1 = ((RenameClassRefactoring) refactoring1).getRenamedClassName();
            String class2 = ((RenameClassRefactoring) refactoring2).getOriginalClassName();
            return class1.equals(class2);
        }
        else if(type1 == RefactoringType.RENAME_CLASS && type2 == RefactoringType.RENAME_METHOD) {
            // Check if they happen in the same commit
            String class1 = ((RenameClassRefactoring) refactoring1).getOriginalClassName();
            String class2 = ((RenameOperationRefactoring) refactoring2).getOriginalOperation().getClassName();
            if(class1.equals(class2)) {
                return true;
            }
            // Check if the method is renamed in a commit after the class is renamed
            class1 = ((RenameClassRefactoring) refactoring1).getRenamedClassName();
            return class1.equals(class2);
        }
        else if(type1 == RefactoringType.RENAME_METHOD && type2 == RefactoringType.RENAME_CLASS) {
            // Do the same thing as above
            String class1 = ((RenameOperationRefactoring) refactoring1).getOriginalOperation().getClassName();
            String class2 = ((RenameClassRefactoring) refactoring2).getOriginalClassName();
            if(class1.equals(class2)) {
                return true;
            }
            class2 = ((RenameClassRefactoring) refactoring2).getRenamedClassName();
            return class1.equals(class2);
        }
        return false;

    }

    public void updateGraph(Node node, Node dependentNode) {
        dependentNode.addToDependsList(node);
        graph.addEdge(node, dependentNode);

    }


}
