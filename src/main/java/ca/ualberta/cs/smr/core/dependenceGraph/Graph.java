package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    Project project;
    private List<Node> nodes;

    public Graph(Project project) {
        this.project = project;
    }

    public void createGraph(List<Pair> pairs) {
        this.nodes = new ArrayList<>();
        if(pairs.size() == 0) {
            return;
        }
        if(pairs.size() == 1) {
            Node node = new Node(pairs.get(0).getValue());
            addNode(node);
            return;
        }
        for(int i = pairs.size() - 1; i > -1; i--) {
            Pair pair = pairs.get(i);
            Refactoring refactoring = pair.getValue();
            Node node = new Node(refactoring);
            insertNode(node);
            addNode(node);
        }

    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEdge(Node from, int weight, Node to) {
        Edge edge = new Edge(from, weight, to);
        from.addEdge(edge);
    }

    void insertNode(Node newNode) {
        Node temp = null;
        for(Node node : nodes) {
            if(hasDependence(node, newNode)) {
                if(!node.hasNeighbors()) {
                    addEdge(node, 1, newNode);
                    return;
                }
                else {
                    temp = node;
                }
            }
            else {
                if(node.hasNeighbors() || newNode.hasNeighbors()) {
                    continue;
                }
                if(temp != null) {
                    addEdge(temp, 1, newNode);
                }
                else {
                    addEdge(node, 0, newNode);
                }
            }
        }

    }

    public boolean hasDependence(Node node1, Node node2) {
        Refactoring refactoring1 = node1.getRefactoring();
        Refactoring refactoring2 = node2.getRefactoring();
        if(refactoring1.getRefactoringType() == refactoring2.getRefactoringType()) {
            return false;
        }
        Matrix matrix = new Matrix(project);
        RefactoringElement element = matrix.makeElement(refactoring1);
        RefactoringVisitor visitor = matrix.makeVisitor(refactoring2);
        element.accept(visitor);

        return visitor.getDependenceResult();

    }

    public void printGraph() {

        for(Node node : nodes) {
            for(Edge edge : node.getEdges()) {
                if(edge.getWeight() == 1) {
                    System.out.println(edge.getSource().getRefactoring().toString() +
                            " <== " + edge.getDestination().getRefactoring().toString());
                }
            }
            for(Edge edge : node.getEdges()) {
                if(edge.getWeight() == 0) {
                    System.out.println(edge.getSource().getRefactoring().toString() +
                            " <-- " + edge.getDestination().getRefactoring().toString());
                }
            }
        }

    }


}
