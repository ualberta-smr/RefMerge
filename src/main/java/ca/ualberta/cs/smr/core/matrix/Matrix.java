package ca.ualberta.cs.smr.core.matrix;


import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.RefactoringType;

import java.util.HashMap;
import java.util.List;

/*
 * The logic and dispatching for the conflict matrix happens in Matrix.
 */

public class Matrix {
    final Project project;
    Graph graph;

    static final HashMap<RefactoringType, RefactoringElement> elementMap =
                                                    new HashMap<RefactoringType, RefactoringElement>() {{
       put(RefactoringType.RENAME_METHOD, new RenameMethodElement());
       put(RefactoringType.RENAME_CLASS, new RenameClassElement());
    }};

    static final HashMap<RefactoringType, RefactoringVisitor> visitorMap =
                                                    new HashMap<RefactoringType, RefactoringVisitor>() {{
        put(RefactoringType.RENAME_METHOD, new RenameMethodVisitor());
        put(RefactoringType.RENAME_CLASS, new RenameClassVisitor());
    }};

    public Matrix(Project project) {
        this.project = project;
        this.graph = new Graph(project);
    }

    /*
     * Iterate through each of the left refactorings to compare against the right refactorings.
     */
    public Graph runMatrix(List<Pair> leftPairs, List<Pair> rightPairs) {
        if(leftPairs != null && rightPairs == null) {
            graph.createPartialGraph(leftPairs);
            return graph;
        }
        if(rightPairs != null && leftPairs == null) {
            graph.createPartialGraph(rightPairs);
            return graph;
        }
        graph.createLeftPartialGraph(leftPairs);
        graph.createRightPartialGraph(rightPairs);
        List<Node> leftNodes = graph.getLeftNodes();
        List<Node> rightNodes = graph.getRightNodes();
        for(Node leftNode : leftNodes) {
            compareRefactorings(leftNode, rightNodes);
        }
        return graph;
    }

    /*
     * This calls dispatch for each pair of refactorings to check for conflicts.
     */
    void compareRefactorings(Node leftNode, List<Node> rightNodes) {
        for(Node rightNode : rightNodes) {
            dispatch(leftNode, rightNode);
        }
    }

    /*
     * Perform double dispatch to check if the two refactoring elements conflict.
     */
    void dispatch(Node leftNode, Node rightNode) {
        // Get the refactoring types so we can create the corresponding element and visitor
        RefactoringElement element = makeElement(leftNode);
        RefactoringVisitor visitor = makeVisitor(rightNode);
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

    public RefactoringVisitor makeVisitor(Node node) {
        RefactoringType type = node.getRefactoring().getRefactoringType();
        RefactoringVisitor visitor = visitorMap.get(type);
        visitor.set(node, graph);
        return visitor;
    }

}
