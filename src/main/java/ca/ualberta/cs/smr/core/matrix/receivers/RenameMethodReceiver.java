package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameMethodRenameMethodCell;

public class RenameMethodReceiver extends Receiver {

    /*
     * Check if rename method conflicts with rename method
     */
    @Override
    public void receive(RenameMethodElement element) {
        Node dispatcherNode = element.getNode();
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
        boolean isConflicting = cell.renameMethodRenameMethodConflictCell(dispatcherNode, receiverNode);
        System.out.println("Rename Method/Rename Method conflict: " + isConflicting);
    }

}
