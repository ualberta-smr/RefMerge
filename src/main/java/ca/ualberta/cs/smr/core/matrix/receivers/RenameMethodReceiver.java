package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameMethodRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class RenameMethodReceiver extends Receiver {

    /*
     * If the project is null, check for rename method/rename method transitivity. If it is not null, check for
     * rename method/rename method conflict.
     */
    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        // Check for rename method/rename method transitivity
        if(dispatcher.getProject() == null) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
            this.isTransitive = cell.checkRenameMethodRenameMethodTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for rename method/rename method conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(project);
            boolean isConflicting = cell.renameMethodRenameMethodConflictCell(dispatcherRefactoring, this.refactoringObject);
            System.out.println("Rename Method/Rename Method conflict: " + isConflicting);
        }
    }

}
