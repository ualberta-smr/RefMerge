package ca.ualberta.cs.smr.core.dependenceGraph;

import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.refactoringminer.api.Refactoring;

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
            Refactoring refactoring = pairs.get(0).getValue();
            Node node = new Node(refactoring);
            tempGraph.addVertex(node);
            Graphs.addGraph(graph, tempGraph);
            return tempGraph;
        }
        Pair pair = pairs.get(pairs.size() - 1);
        Refactoring refactoring = pair.getValue();
        Node previousNode = new Node(refactoring);
        this.tempGraph.addVertex(previousNode);

        ArrayList<Node> nodes = new ArrayList<>();
        for(int i = pairs.size() - 2; i > -1; i--) {
            nodes.add(previousNode);
            pair = pairs.get(i);
            refactoring = pair.getValue();
            Node node = new Node(refactoring);
            this.tempGraph.addVertex(node);
            this.tempGraph.addEdge(previousNode, node);
            node.addDependsList(nodes);
            previousNode = node;
        }
        Graphs.addGraph(graph, tempGraph);
        return tempGraph;
    }

    public List<Node> getSortedNodes() {
        List<Node> nodes = new ArrayList<>();
        TopologicalOrderIterator<Node, DefaultEdge> iterator = new TopologicalOrderIterator<>(graph);
        while(iterator.hasNext()) {
            Node node = iterator.next();
            nodes.add(node);
        }
        return nodes;
    }

    public void updateGraph(Node node, Node dependentNode) {
        graph.addEdge(node, dependentNode);


    }

    public void addVertex(Node node) {
        this.graph.addVertex(node);
    }


}
