package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;
import static ca.ualberta.cs.smr.utils.MatrixUtils.getRefactoredClassOperationName;

/*
 * Contains the logic check for rename class/rename class refactoring conflict.
 */
public class RenameClassRenameClassCell {

    /*
     *  Check if a conflict exists between rename class/rename class refactorings. The conflict that can exist is a
     *  naming conflict.
     *  @param dispatcherNode: A node containing the dispatcher rename class refactoring.
     *  @param receiverNode: A node containing the receiver rename class refactoring.
     */
    public static boolean renameClassRenameClassConflictCell(Node dispatcherNode, Node receiverNode) {
        if(checkClassNamingConflict(dispatcherNode, receiverNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }


    public static boolean checkClassNamingConflict(Node dispatcherNode, Node receiverNode) {
        Refactoring dispatcherRef = dispatcherNode.getRefactoring();
        Refactoring receiverRef = receiverNode.getRefactoring();
        // Get the package for each class
        String dispatcherPackage = getOriginalClassPackage(dispatcherRef);
        String receiverPackage = getOriginalClassPackage(receiverRef);
        // Check that the classes are in the same package
        if(!isSameName(dispatcherPackage, receiverPackage)) {
            return false;
        }
        String dispatcherOriginalClassName = getOriginalClassOperationName(dispatcherRef);
        String receiverOriginalClassName = getOriginalClassOperationName(receiverRef);
        String dispatcherNewClassName = getRefactoredClassOperationName(dispatcherRef);
        String receiverNewClassName = getRefactoredClassOperationName(receiverRef);

        return checkNamingConflict(dispatcherOriginalClassName, receiverOriginalClassName,
                dispatcherNewClassName, receiverNewClassName);
    }
}
