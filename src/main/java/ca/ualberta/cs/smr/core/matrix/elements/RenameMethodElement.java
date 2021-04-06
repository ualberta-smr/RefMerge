package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameMethodRenameMethodCell;
import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;
import com.intellij.openapi.project.Project;


/*
 * Checks if visitor refactorings confict with a rename method refactoring.
 */
public class RenameMethodElement extends RefactoringElement {
    Project project;
    Node elementNode;

    @Override
    public void accept(Receiver r) {
        r.receive(this);
    }

    public void set(Node elementNode, Project project) {
        this.elementNode = elementNode;
        this.project = project;
    }

    /*
     *  Check if a rename method refactoring conflicts with a second rename method refactoring.
     */
    public boolean checkRenameMethodConflict(Node visitorNode) {
        RenameMethodRenameMethodCell renameMethodRenameMethodCell = new RenameMethodRenameMethodCell(project);
        // Check for a method override conflict
        if(renameMethodRenameMethodCell.checkOverrideConflict(elementNode, visitorNode)) {
            System.out.println("Override conflict");
            return true;
        }
        // Check for method overload conflict
        else if(renameMethodRenameMethodCell.checkOverloadConflict(elementNode, visitorNode)) {
            System.out.println("Overload conflict");
            return true;
        }
        // Check for naming conflict

        else if(renameMethodRenameMethodCell.checkMethodNamingConflict(elementNode, visitorNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

}
