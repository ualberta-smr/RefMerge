package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import org.refactoringminer.api.Refactoring;

public class RenameClassVisitor extends RefactoringVisitor {
    Refactoring visitorRef;
    Graph graph;


    public void set(Refactoring visitorRef, Graph graph) {
        this.visitorRef = visitorRef;
        this.graph = graph;

    }

    @Override
    public void visit(RenameMethodElement renameMethod) {
        Refactoring elementRef = renameMethod.checkRenameClassDependence(visitorRef);
        if(elementRef != null) {
            graph.updateGraph(elementRef, visitorRef);
        }
    }

    @Override
    public void visit(RenameClassElement renameClass) {
        boolean foundConflict = renameClass.checkRenameClassConflict(visitorRef);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }
}
