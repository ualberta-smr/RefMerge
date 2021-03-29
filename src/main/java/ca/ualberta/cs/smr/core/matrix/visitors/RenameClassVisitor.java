package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;

public class RenameClassVisitor extends RefactoringVisitor {

    @Override
    public void visit(RenameClassElement renameClass) {
        // Check for rename class/rename class conflict if checking between branches
        boolean foundConflict = renameClass.checkRenameClassConflict(visitorNode);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }

    @Override
    public void visit(ExtractMethodElement extractMethod) {

    }
}
