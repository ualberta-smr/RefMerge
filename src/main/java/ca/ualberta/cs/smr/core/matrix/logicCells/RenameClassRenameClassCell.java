package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import org.refactoringminer.api.Refactoring;

import static ca.ualberta.cs.smr.utils.MatrixUtils.*;
import static ca.ualberta.cs.smr.utils.MatrixUtils.getRefactoredClassOperationName;

public class RenameClassRenameClassCell {

    public static boolean renameClassRenameClassConflictCell(Node elementNode, Node visitorNode) {
        if(RenameClassRenameClassCell.checkClassNamingConflict(elementNode, visitorNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }


    public static boolean checkClassNamingConflict(Node elementNode, Node visitorNode) {
        Refactoring elementRef = elementNode.getRefactoring();
        Refactoring visitorRef = visitorNode.getRefactoring();
        // Get the package for each class
        String elementPackage = getOriginalClassPackage(elementRef);
        String visitorPackage = getOriginalClassPackage(visitorRef);
        // Check that the classes are in the same package
        if(!isSameName(elementPackage, visitorPackage)) {
            return false;
        }
        String elementOriginalClassName = getOriginalClassOperationName(elementRef);
        String visitorOriginalClassName = getOriginalClassOperationName(visitorRef);
        String elementNewClassName = getRefactoredClassOperationName(elementRef);
        String visitorNewClassName = getRefactoredClassOperationName(visitorRef);

        return checkNamingConflict(elementOriginalClassName, visitorOriginalClassName,
                elementNewClassName, visitorNewClassName);
    }
}
