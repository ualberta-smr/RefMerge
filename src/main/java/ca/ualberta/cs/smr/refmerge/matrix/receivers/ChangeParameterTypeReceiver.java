package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ChangeParameterTypeMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ReorderParameterMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class ChangeParameterTypeReceiver extends Receiver {

    /*
     * Check for change parameter type / move + rename method combination only to add to move + rename method resilience
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ChangeParameterTypeMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

}
