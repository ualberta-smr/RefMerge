package ca.ualberta.cs.smr.refmerge.matrix.dispatcher;

import ca.ualberta.cs.smr.refmerge.matrix.receivers.Receiver;


/*
 * Dispatches the rename method refactoring to the corresponding receiver.
 */
public class MoveRenameMethodDispatcher extends RefactoringDispatcher {
    @Override
    public void dispatch(Receiver r) {
        r.receive(this);
    }

}
