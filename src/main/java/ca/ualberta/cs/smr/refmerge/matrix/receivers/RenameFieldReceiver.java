package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.InlineMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;


public class RenameFieldReceiver extends Receiver {
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        // Placeholder for if rename field/R+M method can result in conflicts/dependence
    }

    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        // Placeholder for if rename field/R+M class can result in conflicts/dependence
    }

    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        // Placeholder for if rename field/extract method can result in conflicts/dependence
    }

    @Override
    public void receive(InlineMethodDispatcher dispatcher) {
        // Placeholder for if rename field/inline method can result in conflicts/dependence
    }}
