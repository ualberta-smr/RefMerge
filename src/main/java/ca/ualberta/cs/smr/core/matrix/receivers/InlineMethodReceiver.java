package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.InlineMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;

public class InlineMethodReceiver extends Receiver {
    /*
     *  Check if we can simplify inline method/move+rename method and update or if there is conflict/dependence.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {

    }

    /*
     * Check for transitivity and dependence in inline method/move+rename class and update.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {

    }

    /*
     * Check for inline method/extract method conflicts.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {

    }

    /*
     * Check for inline method/inline method dependence
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {

    }
}
