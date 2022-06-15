package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PullUpMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.PushDownMethodDispatcher;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ChangeParameterTypeExtractMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ChangeParameterTypeMoveRenameMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ChangeParameterTypePullUpMethodCell;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.ChangeParameterTypePushDownMethodCell;
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

    /*
     * Check for change parameter type / extract method combination only to add to extract method resilience
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ChangeParameterTypeExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }


    /*
     * Check for change parameter type / pull up method combination only to add to pull up method resilience
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ChangeParameterTypePullUpMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * Check for change parameter type / push down method combination only to add to push down method resilience
     */
    @Override
    public void receive(PushDownMethodDispatcher dispatcher) {
        RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = false;
            // There is no opportunity for transitivity in this case. There is only a combination case that can occur
            ChangeParameterTypePushDownMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

}
