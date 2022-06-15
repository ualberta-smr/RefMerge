package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.InlineMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ReorderParameterExtractMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ReorderParameterInlineMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ReorderParameterMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ReorderParameterPullUpMethodCell;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class ReorderParameterReceiver extends Receiver {

    /*
     * Check for reorder parameter / move + rename method combination only to add to move + rename method resilience
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ReorderParameterMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for reorder parameter / extract method combination only to add to extract method resilience
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ReorderParameterExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for reorder parameter / inline method combination only to add to inline method resilience
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ReorderParameterInlineMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for reorder parameter / inline method combination only to add to inline method resilience
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ReorderParameterPullUpMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }


}
