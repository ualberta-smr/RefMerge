package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.InlineMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.InlineMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameMethodMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class InlineMethodReceiver extends Receiver {
    /*
     *  Check if we can simplify inline method/move+rename method and update or if there is conflict/dependence.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        // Check for inline method/move+rename method combination
        if(dispatcher.isSimplify()) {
            RefactoringObject moveRenameMethod = dispatcher.getRefactoringObject();
            InlineMethodMoveRenameMethodCell.checkCombination(moveRenameMethod, this.refactoringObject);
            dispatcher.setRefactoringObject(moveRenameMethod);
        }
        // Check for inline method/move+rename method conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            boolean isConflicting = InlineMethodMoveRenameMethodCell.conflictCell(dispatcherRefactoring, this.refactoringObject);
            this.isConflicting = isConflicting;
            if(!isConflicting) {
                boolean isDependent = InlineMethodMoveRenameMethodCell.dependenceCell(dispatcherRefactoring, this.refactoringObject);
                if (isDependent) {
                    // If they are dependent, update the inline method refactoring
                    this.refactoringObject.setDestinationFilePath(dispatcherRefactoring.getDestinationFilePath());
                    ((InlineMethodObject) this.refactoringObject)
                            .setDestinationClassName(((MoveRenameMethodObject) dispatcherRefactoring).getDestinationClassName());
                    ((InlineMethodObject) this.refactoringObject)
                            .setDestinationMethodSignature(((MoveRenameMethodObject) dispatcherRefactoring).getDestinationMethodSignature());
                    dispatcher.setRefactoringObject(dispatcherRefactoring);
                }
            }
        }
    }

    /*
     * Check for transitivity and dependence in inline method/move+rename class and update.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {

    }

    /*
     * Check for inline method/extract method conflicts.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {

    }

    /*
     * Check for inline method/inline method dependence
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {

    }
}
