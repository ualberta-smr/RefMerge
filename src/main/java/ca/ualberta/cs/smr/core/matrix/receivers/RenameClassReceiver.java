package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;

public class RenameClassReceiver extends Receiver {

    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        Node dispatcherNode = dispatcher.getNode();
        boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(dispatcherNode, receiverNode);
        if (isDependent) {
            // If there is dependence between branches, the rename method needs to happen before the rename class
            graph.updateGraph(dispatcherNode, receiverNode);
        }
    }

    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        Node dispatcherNode = dispatcher.getNode();
        boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(dispatcherNode, receiverNode);
        System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
    }

}
