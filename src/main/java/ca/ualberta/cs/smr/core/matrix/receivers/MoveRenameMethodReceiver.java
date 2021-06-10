package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class MoveRenameMethodReceiver extends Receiver {

    /*
     * If simplify is true, check for rename method/rename method transitivity. If it is not null, check for
     * rename method/rename method conflict.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        // Check for rename method/rename method transitivity
        if(dispatcher.isSimplify()) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
            this.isTransitive = cell.checkRenameMethodRenameMethodTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for rename method/rename method conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            MoveRenameMethodMoveRenameMethodCell cell = new MoveRenameMethodMoveRenameMethodCell(project);
            boolean isConflicting = cell.renameMethodRenameMethodConflictCell(dispatcherRefactoring, this.refactoringObject);
            System.out.println("Rename Method/Rename Method conflict: " + isConflicting);
        }
    }

}
