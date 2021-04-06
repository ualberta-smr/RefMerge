package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;


/*
 * Checks if visitor refactorings confict with a rename method refactoring.
 */
public class RenameMethodElement extends RefactoringElement {
    @Override
    public void accept(Receiver r) {
        r.receive(this);
    }

}
