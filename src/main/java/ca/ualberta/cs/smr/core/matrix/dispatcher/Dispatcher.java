package ca.ualberta.cs.smr.core.matrix.dispatcher;

import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;

public interface Dispatcher {
    void dispatch(Receiver r);
}

