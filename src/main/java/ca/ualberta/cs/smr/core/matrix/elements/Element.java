package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;

public interface Element {
    void accept(Visitor v);
}

