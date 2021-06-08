package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.ExtractMethodRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class ExtractMethodReceiver extends Receiver {


    /*
     * If the project is null, check if we can simplify extract method/rename method and update.
     */
    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        if(dispatcher.getProject() == null) {
            RefactoringObject renameMethod = dispatcher.getRefactoringObject();
            // Need to use this.refactoringObject/dispatcher.refactoringObject or set dispatcher.refactoringObject
            this.isTransitive = ExtractMethodRenameMethodCell.checkExtractMethodRenameMethodTransitivity(renameMethod,
                    this.refactoringObject);
            dispatcher.setRefactoringObject(renameMethod);
        }

    }

    /*
     * If the project is null, check if we can simplify extract method/rename class and update.
     */
    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        if(dispatcher.getProject() == null) {
            RefactoringObject renameClass = dispatcher.getRefactoringObject();
            ExtractMethodRenameClassCell.checkExtractMethodRenameClassCombination(renameClass, this.refactoringObject);
            dispatcher.setRefactoringObject(renameClass);
        }
    }

    /*
     * If the project is null, check if we can simplify extract method/extract method and update.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {

    }
}
