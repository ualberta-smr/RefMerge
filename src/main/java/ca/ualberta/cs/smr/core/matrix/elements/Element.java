package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;

public interface Element {
    void accept(Receiver r);
}

