package ca.ualberta.cs.smr.refmerge.matrix.receivers;

import ca.ualberta.cs.smr.refmerge.matrix.dispatcher.*;
import ca.ualberta.cs.smr.refmerge.matrix.logicCells.*;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;

public class RenamePackageReceiver extends Receiver {

    /*
     * If simplify is true, check for rename package / rename package transitivity. If it is false, check for
     * rename package / rename package conflict.
     */
    @Override
    public void receive(RenamePackageDispatcher dispatcher) {
        // Check for rename package / rename package transitivity
        if(dispatcher.isSimplify()) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            // Dispatcher refactoring is always the second refactoring when dealing with two refactorings of the same type
            this.isTransitive = RenamePackageRenamePackageCell.checkTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);
        }
        // Check for rename package / rename package conflict
        else {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            boolean isConflicting = RenamePackageRenamePackageCell
                    .conflictCell(dispatcherRefactoring, this.refactoringObject);
            this.isConflicting = isConflicting;
            if(isConflicting) {
                dispatcherRefactoring.setReplayFlag(false);
                this.refactoringObject.setReplayFlag(false);
            }

        }
    }

    /*
     * If simplify is true, check for rename package / move + rename method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageMoveRenameMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / extract method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(ExtractMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageExtractMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / inline method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(InlineMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageInlineMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / move+rename class combination. There are no possible conflicts
     * between package and class level.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageMoveRenameClassCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / move+rename field combination. There are no possible conflicts
     * between package and field level.
     */
    @Override
    public void receive(MoveRenameFieldDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackageMoveRenameFieldCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / pull up method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(PullUpMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackagePullUpMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / push down method combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(PushDownMethodDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackagePushDownMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / pull up field combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(PullUpFieldDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackagePushDownMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }

    /*
     * If simplify is true, check for rename package / push down field combination. There are no possible conflicts
     * between package and method level.
     */
    @Override
    public void receive(PushDownFieldDispatcher dispatcher) {
        if(dispatcher.isSimplify()) {
            RefactoringObject dispatcherRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenamePackagePushDownMethodCell.checkCombination(dispatcherRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(dispatcherRefactoring);
        }
    }


}
