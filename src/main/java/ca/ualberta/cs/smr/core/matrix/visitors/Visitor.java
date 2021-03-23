package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

public interface Visitor {
    void visit(RenameMethodElement renameMethod);
    void visit(RenameClassElement renameClass);
    void visit(ExtractMethodElement extractMethod);
}
