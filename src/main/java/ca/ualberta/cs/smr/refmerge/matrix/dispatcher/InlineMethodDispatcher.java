package ca.ualberta.cs.smr.refmerge.matrix.dispatcher;

import ca.ualberta.cs.smr.refmerge.matrix.receivers.Receiver;

public class InlineMethodDispatcher extends RefactoringDispatcher {
    @Override
    public void dispatch(Receiver r) {
        r.receive(this);
    }
}
