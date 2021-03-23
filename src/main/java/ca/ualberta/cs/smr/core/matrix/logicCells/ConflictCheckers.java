package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;



import static ca.ualberta.cs.smr.utils.MatrixUtils.*;


public class ConflictCheckers {
    Project project;

    public boolean checkClassNamingConflict(Node elementNode, Node visitorNode) {
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

    public boolean checkNamingConflict(String elementOriginal, String visitorOriginal, String elementNew,
                                                    String visitorNew) {
        // If the original method names are equal but the destination names are not equal, check for conflict
        if(isSameName(elementOriginal, visitorOriginal) && !isSameName(elementNew, visitorNew)) {
            return true;
        }
        // If the original method names are not equal but the destination names are equal
        else return !isSameName(elementOriginal, visitorOriginal) && isSameName(elementNew, visitorNew);
    }

}


