package ca.ualberta.cs.smr.core.matrix.dispatcher;

import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;


/*
 * Dispatches the rename method refactoring to the corresponding receiver.
 */
public class MoveRenameMethodDispatcher extends RefactoringDispatcher {
    @Override
    public void dispatch(Receiver r) {
        r.receive(this);
    }

}
