package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.InlineMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.AddParameterExtractMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.AddParameterInlineMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.AddParameterMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class AddParameterReceiver extends Receiver {

    /*
     * Check for add parameter / move + rename method combination only to add to move + rename method resilience
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            AddParameterMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for add parameter / extract method combination only to add to move + rename method resilience
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            AddParameterExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for add parameter / inline method combination only to add to move + rename method resilience
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            AddParameterInlineMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }


}
