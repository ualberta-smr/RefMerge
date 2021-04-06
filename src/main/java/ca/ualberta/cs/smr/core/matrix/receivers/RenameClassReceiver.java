package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;

public class RenameClassReceiver extends Receiver {

    @Override
    public void receive(RenameMethodElement element) {
        Node dispatcherNode = element.getNode();
        boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(dispatcherNode, receiverNode);
        if (isDependent) {
            // If there is dependence between branches, the rename method needs to happen before the rename class
            graph.updateGraph(receiverNode, dispatcherNode);
        }
    }

    @Override
    public void receive(RenameClassElement element) {
        Node dispatcherNode = element.getNode();
        boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(dispatcherNode, receiverNode);
        System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
    }

}
