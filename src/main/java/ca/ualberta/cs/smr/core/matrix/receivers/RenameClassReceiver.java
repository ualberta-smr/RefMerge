package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;

public class RenameClassReceiver extends Receiver {

    /*
     * If the project is null, check if we can simplify rename class/rename method and update. If it is not null, check for
     * rename method/rename method dependence.
     */
    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        RefactoringObject methodRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.getProject() == null) {
            RenameClassRenameMethodCell.checkRenameClassRenameMethodCombination(methodRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(methodRefactoring);
        }
        else {
            boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(methodRefactoring, this.refactoringObject);
        }
    }

    /*
     * If the project is null, check for rename class/rename class transitivity. If it is not null, check for
     * rename class/rename class conflict.
     */
    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.getProject() == null) {
            this.isTransitive = RenameClassRenameClassCell.checkRenameClassRenameClassTransitivity(this.refactoringObject,
                    secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);

        }
        else {
            boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(secondRefactoring, this.refactoringObject);
            System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
        }
    }

}
