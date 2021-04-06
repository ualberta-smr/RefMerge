package ca.ualberta.cs.smr.core.matrix;


import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RefactoringDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.receivers.ExtractMethodReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameClassReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameMethodReceiver;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.refactoringminer.api.RefactoringType;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/*
 * Creates the dependence graph for the refactoring lists and dispatches to the corresponding logic cell for each pair
 * of refactorings.
 */

public class Matrix {
    final Project project;
    DependenceGraph graph;

    static final HashMap<RefactoringType, RefactoringDispatcher> dispatcherMap =
                                                    new HashMap<RefactoringType, RefactoringDispatcher>() {{
       put(RefactoringType.RENAME_METHOD, new RenameMethodDispatcher());
       put(RefactoringType.RENAME_CLASS, new RenameClassDispatcher());
       put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodDispatcher());
    }};

    static final HashMap<RefactoringType, Receiver> receiverMap =
                                                    new HashMap<RefactoringType, Receiver>() {{
        put(RefactoringType.RENAME_METHOD, new RenameMethodReceiver());
        put(RefactoringType.RENAME_CLASS, new RenameClassReceiver());
        put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodReceiver());
    }};

    public Matrix(Project project) {
        this.project = project;
        this.graph = new DependenceGraph(project);
    }

    public Matrix(Project project, DependenceGraph graph) {
        this.project = project;
        this.graph = graph;
    }

    /*
     * Iterate through each of the left refactorings to compare against the right refactorings.
     */
    public DependenceGraph runMatrix(List<Pair> leftPairs, List<Pair> rightPairs) {
        if(!leftPairs.isEmpty() && rightPairs.isEmpty()) {
            graph.createPartialGraph(leftPairs);
            return graph;
        }
        if(!rightPairs.isEmpty() && leftPairs.isEmpty()) {
            graph.createPartialGraph(rightPairs);
            return graph;
        }
        if(leftPairs.isEmpty()) {
            return null;
        }
        DefaultDirectedGraph<Node, DefaultEdge> leftGraph = graph.createPartialGraph(leftPairs);
        DefaultDirectedGraph<Node, DefaultEdge> rightGraph = graph.createPartialGraph(rightPairs);
        DepthFirstIterator<Node, DefaultEdge> leftIterator = new DepthFirstIterator<>(leftGraph);
        DepthFirstIterator<Node, DefaultEdge> rightIterator = new DepthFirstIterator<>(rightGraph);
        while(leftIterator.hasNext()) {
            Node leftNode = leftIterator.next();
            compareRefactorings(leftNode, rightIterator);
        }
        return graph;
    }

    /*
     * This calls dispatch for each pair of refactorings to check for conflicts.
     */
    void compareRefactorings(Node leftNode, DepthFirstIterator<Node, DefaultEdge> rightIterator) {
        while(rightIterator.hasNext()) {
            Node rightNode = rightIterator.next();
            dispatch(leftNode, rightNode);
        }
    }

    /*
     * Perform double dispatch to check if the two refactoring elements conflict.
     */
    void dispatch(Node leftNode, Node rightNode) {
        // Get the refactoring types so we can create the corresponding dispatcher and receiver

        int leftValue = getRefactoringValue(leftNode.getRefactoring().getRefactoringType());
        int rightValue = getRefactoringValue(rightNode.getRefactoring().getRefactoringType());

        RefactoringDispatcher dispatcher;
        Receiver receiver;
        if(leftValue < rightValue) {
            dispatcher = makeDispatcher(rightNode);
            receiver = makeReceiver(leftNode);
        }
        else {
            dispatcher = makeDispatcher(leftNode);
            receiver = makeReceiver(rightNode);
        }
        dispatcher.dispatch(receiver);
    }

    /*
     * Use the refactoring type to get the refactoring dispatcher class from the dispatcherMap.
     * Set the node field in the dispatcher.
     */
    public RefactoringDispatcher makeDispatcher(Node node) {
        RefactoringType type = node.getRefactoring().getRefactoringType();
        RefactoringDispatcher dispatcher = dispatcherMap.get(type);
        dispatcher.set(node, project);
        return dispatcher;
    }

    /*
     * Use the refactoring type to get the refactoring receiver class from the receiverMap.
     * Set the node field in the receiver and get an instance of the graph so we can update it.
     */
    public Receiver makeReceiver(Node node) {
        RefactoringType type = node.getRefactoring().getRefactoringType();
        Receiver receiver = receiverMap.get(type);
        receiver.set(node, graph, project);
        return receiver;
    }

    /*
     * Get the ordered refactoring value of the refactoring type. We need to update this method each time we add a new
     * refactoring type.
     */
    protected int getRefactoringValue(RefactoringType refactoringType) {
        Vector<RefactoringType> vector = new Vector<>();
        vector.add(RefactoringType.RENAME_METHOD);
        vector.add(RefactoringType.RENAME_CLASS);
        vector.add(RefactoringType.EXTRACT_OPERATION);

        Enumeration<RefactoringType> enumeration = vector.elements();
        int value = 0;
        while(enumeration.hasMoreElements()) {
            value++;
            if(refactoringType.equals(enumeration.nextElement())) {
                return value;
            }
        }
        return -1;
    }

}
