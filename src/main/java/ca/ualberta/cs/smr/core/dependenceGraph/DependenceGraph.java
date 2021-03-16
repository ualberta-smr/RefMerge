package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.core.matrix.Matrix;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        DepthFirstIterator<Node, DefaultEdge> dFI = new DepthFirstIterator<>(tempGraph);
        while(dFI.hasNext()) {
            Node nodeInGraph = dFI.next();
            hasDependence(nodeInGraph, node);
        }

    }

    public List<Node> getSortedNodes() {
        List<Node> nodes = new ArrayList<>();
        DepthFirstIterator<Node, DefaultEdge> iterator = new DepthFirstIterator<>(this.graph);
        while(iterator.hasNext()) {
            nodes.add(iterator.next());
        }
        Set<DefaultEdge> edges = this.graph.edgeSet();
        for(DefaultEdge edge : edges) {
            System.out.println(edge.toString());
        }
        return nodes;
    }

    public void hasDependence(Node node1, Node node2) {
        Matrix matrix = new Matrix(project, this);
        RefactoringElement element = matrix.makeElement(node1);
        RefactoringVisitor visitor = matrix.makeVisitor(node2);
        element.accept(visitor);

    }

    public boolean containsVertex(Node node) {
        return this.graph.containsVertex(node);
    }

    public void updateGraph(Node node, Node dependentNode) {
        if(this.graph.containsVertex(dependentNode)) {
            dependentNode.addToDependsList(node);
            graph.addEdge(node, dependentNode);
        }
        else {
            this.tempGraph.addEdge(node, dependentNode);
        }


    }

    public void addVertex(Node node) {
        this.graph.addVertex(node);
    }


}
