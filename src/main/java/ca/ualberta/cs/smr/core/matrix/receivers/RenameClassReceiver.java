package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.elements.ExtractMethodElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;

public class RenameClassReceiver extends Receiver {

    @Override
    public void receive(RenameClassElement renameClass) {
        // Check for rename class/rename class conflict if checking between branches
        boolean foundConflict = renameClass.checkRenameClassConflict(receiverNode);
        System.out.println("Rename Class/Rename Class conflict: " + foundConflict);
    }

    @Override
    public void receive(ExtractMethodElement extractMethod) {

    }
}
