package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
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
        if(dispatcher.getProject() == null) {
            RefactoringObject methodRefactoring = dispatcher.getRefactoringObject();
            RenameClassRenameMethodCell.checkRenameClassRenameMethodCombination(methodRefactoring, this.refactoringObject);
            dispatcher.setRefactoringObject(methodRefactoring);
        }
        else {
            Node dispatcherNode = dispatcher.getNode();
            boolean isDependent = RenameClassRenameMethodCell.renameClassRenameMethodDependenceCell(dispatcherNode, receiverNode);
            if (isDependent) {
                // If there is dependence between branches, the rename method needs to happen before the rename class
                graph.updateGraph(dispatcherNode, receiverNode);
            }
        }
    }

    /*
     * If the project is null, check for rename class/rename class transitivity. If it is not null, check for
     * rename class/rename class conflict.
     */
    @Override
    public void receive(RenameClassDispatcher dispatcher) {
        if(dispatcher.getProject() == null) {
            RefactoringObject secondRefactoring = dispatcher.getRefactoringObject();
            this.isTransitive = RenameClassRenameClassCell.checkRenameClassRenameClassTransitivity(this.refactoringObject,
                    secondRefactoring);
            dispatcher.setRefactoringObject(secondRefactoring);

        }
        else {
            Node dispatcherNode = dispatcher.getNode();
            boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(dispatcherNode, receiverNode);
            System.out.println("Rename Class/Rename Class conflict: " + isConflicting);
        }
    }

}
