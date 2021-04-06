package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;

public class RenameMethodReceiver extends Receiver {

    /*
     * Check if rename method conflicts with rename method
     */
    @Override
    public void receive(RenameMethodElement renameMethod) {
        boolean foundConflict = renameMethod.checkRenameMethodConflict(receiverNode);
        System.out.println("Rename Method/Rename Method conflict: " + foundConflict);
    }

    /*
     * Check if rename class conflicts with rename method
     */
    @Override
    public void receive(RenameClassElement renameClass) {
        Node elementNode = renameClass.checkRenameMethodDependence(receiverNode);
        if (elementNode != null) {
            // If there is dependence between branches, the rename method needs to happen before the rename class
                graph.updateGraph(receiverNode, elementNode);
        }
    }

    @Override
    public void receive(ExtractMethodElement extractMethod) {

    }
}
