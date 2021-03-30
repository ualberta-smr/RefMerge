package ca.ualberta.cs.smr.core.matrix;


import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.ExtractMethodVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
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

    static final HashMap<RefactoringType, RefactoringElement> elementMap =
                                                    new HashMap<RefactoringType, RefactoringElement>() {{
       put(RefactoringType.RENAME_METHOD, new RenameMethodElement());
       put(RefactoringType.RENAME_CLASS, new RenameClassElement());
       put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodElement());
    }};

    static final HashMap<RefactoringType, RefactoringVisitor> visitorMap =
                                                    new HashMap<RefactoringType, RefactoringVisitor>() {{
        put(RefactoringType.RENAME_METHOD, new RenameMethodVisitor());
        put(RefactoringType.RENAME_CLASS, new RenameClassVisitor());
        put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodVisitor());
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
        // Get the refactoring types so we can create the corresponding element and visitor

        int leftValue = getRefactoringValue(leftNode.getRefactoring().getRefactoringType());
        int rightValue = getRefactoringValue(rightNode.getRefactoring().getRefactoringType());

        RefactoringElement element;
        RefactoringVisitor visitor;
        if(leftValue < rightValue) {
            element = makeElement(rightNode);
            visitor = makeVisitor(leftNode);
        }
        else {
            element = makeElement(leftNode);
            visitor = makeVisitor(rightNode);
        }
        element.accept(visitor);
    }

    /*
     * Use the refactoring type to get the refactoring element class from the elementMap.
     * Set the refactoring field in the element.
     */
    public RefactoringElement makeElement(Node node) {
        RefactoringType type = node.getRefactoring().getRefactoringType();
        RefactoringElement element = elementMap.get(type);
        element.set(node, project);
        return element;
    }

    /*
     * Use the refactoring type to get the refactoring visitor class from the visitorMap.
     * Set the refactoring field in the visitor and get an instance of the graph so we can update it.
     */
    public RefactoringVisitor makeVisitor(Node node) {
        RefactoringType type = node.getRefactoring().getRefactoringType();
        RefactoringVisitor visitor = visitorMap.get(type);
        visitor.set(node, graph);
        return visitor;
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
