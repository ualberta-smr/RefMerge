package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.*;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class RenameParameterReceiver extends Receiver {

    /*
     * If performing a same branch comparison, check for rename parameter / rename parameter transitivity.
     * If performing cross-branch comparison, check for naming conflicts.
     */
    @Override
    public void receive(RenameParameterDispatcher dispatcher) {
        RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
        // Check for transitivity
        if(dispatcher.isSimplify()) {
            this.isTransitive = RenameParameterRenameParameterCell.checkTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for naming conflicts
        else {
            boolean isConflicting = RenameParameterRenameParameterCell.conflictCell(secondRefactoring, this.refactoringObject);
            if(isConflicting) {
                this.isConflicting = true;
                secondRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }
        }
    }

    /*
     * If simplify is true, check for rename parameter / move + rename method. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }

    }

    /*
     * If simplify is true, check for rename parameter / extract method. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }

    }

    /*
     * If simplify is true, check for rename parameter / inline method. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterInlineMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }

    }

    /*
     * If simplify is true, check for rename parameter / pull up method. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterPullUpMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }

    }

    /*
     * If simplify is true, check for rename parameter / push down method. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(PushDownMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterPushDownMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename parameter / move + rename class. If it is false, there shouldn't be
     * any conflicts.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameParameterMoveRenameClassCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }


    /*
     * If simplify is true, check for rename parameter / rename package combination. If it is false, there is no
     * conflict to check for between package and parameter level
     */
    @Override
    public void receive(RenamePackageDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            RenameParameterRenamePackageCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

}
