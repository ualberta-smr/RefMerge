package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;

public class RenameClassReceiver extends Receiver {

    /*
     * Check if we can simplify rename class/rename method and update. If we are not checking for transitivity, check for
     * rename method/rename method dependence.
     */
    @Override
    public void receive(RenameMethodDispatcher dispatcher) {
        RefactoringObject methodRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
            RenameClassRenameMethodCell.checkRenameClassRenameMethodCombination(methodRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(methodRefactoring);
        }
        else {
            boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(methodRefactoring, this.refactoringObject);
            // If the rename method happens in the rename class, update the renamed method's class name
            if(isDependent) {
                methodRefactoring.setDestinationFilePath(refactoringObject.getDestinationFilePath());
                ((RenameMethodObject) methodRefactoring).
                        setDestinationClassName(((RenameClassObject) refactoringObject).getDestinationClassName());
            }
        }
    }

    /*
     * If simplify is true, check for rename class/rename class transitivity. If it is not null, check for
     * rename class/rename class conflict.
     */
    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
        if(dispatcher.isSimplify()) {
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
