package ca.ualberta.cs.smr.refmerge.matrix.dispatcher;

import ca.ualberta.cs.smr.refmerge.matrix.receivers.Receiver;

public interface Dispatcher {
    void dispatch(Receiver r);
}

