package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.MoveRenameMethodObject;

public class MoveRenameClassReceiver extends Receiver {

    /*
     * Check if we can simplify rename class/rename method and update. If we are not checking for transitivity, check for
     * rename method/rename method dependence.
     */
    @Override
    public void receive(MoveRenameMethodDispatcher dispatcher) {
        RefactoringObject methodRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            MoveRenameClassMoveRenameMethodCell
                    .checkMoveRenameClassMoveRenameMethodCombination(methodRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(methodRefactoring);
        }
        else {
            boolean isDependent = MoveRenameClassMoveRenameMethodCell
                    .moveRenameClassMoveRenameMethodDependenceCell(methodRefactoring, this.refactoringObject);
            // If the rename method happens in the rename class, update the renamed method's class name
            if(isDependent) {
                methodRefactoring.setDestinationFilePath(refactoringObject.getDestinationFilePath());
                ((MoveRenameMethodObject) methodRefactoring).
                        setDestinationClassName(((MoveRenameClassObject) refactoringObject).getDestinationClassName());
            }
        }
    }

    /*
     * If simplify is true, check for rename class/rename class transitivity. If it is not null, check for
     * rename class/rename class conflict.
     */
    @Override
    public void receive(MoveRenameClassDispatcher dispatcher) {
        RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            this.isTransitive = MoveRenameClassMoveRenameClassCell
                    .checkMoveRenameClassMoveRenameClassTransitivity(this.refactoringObject, secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);

        }
        else {
            boolean isConflicting = MoveRenameClassMoveRenameClassCell
                    .MoveRenameClassMoveRenameClassConflictCell(secondRefactoring, this.refactoringObject);
            System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
        }
    }

}
