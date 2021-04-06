package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;
import static ca.ualberta.cs.smr.utils.MatrixUtils.getRefactoredClassOperationName;

public class RenameClassRenameClassCell {

    public static boolean renameClassRenameClassConflictCell(Node dispatcherNode, Node receiverNode) {
        if(RenameClassRenameClassCell.checkClassNamingConflict(dispatcherNode, receiverNode)) {
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
