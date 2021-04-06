package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;

public class RenameClassElement extends RefactoringElement {

    @Override
    public void accept(Receiver r) {
        r.receive(this);
    }

}
