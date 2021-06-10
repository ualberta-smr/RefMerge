package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameClassObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

/*
 * Contains the logic check for rename class/rename method ordering dependence.
 */
public class RenameClassRenameMethodCell {

    /*
     *  Check if an ordering dependence exists between rename class and rename method refactorings.
     *  @param dispatcherNode: A node containing the dispatcher rename method refactoring.
     *  @param receiverNode: A node containing the receiver rename class refactoring.
     */
    public static boolean renameClassRenameMethodDependenceCell(Node dispatcherNode, Node receiverNode) {
        return checkRenameMethodRenameClassDependence(dispatcherNode, receiverNode);
    }

    public static boolean checkRenameMethodRenameClassDependence(Node methodNode, Node classNode) {
        Refactoring classRefactoring = classNode.getRefactoring();
        Refactoring methodRefactoring = methodNode.getRefactoring();
        String classClass = ((RenameClassRefactoring) classRefactoring).getOriginalClass().getName();
        String methodClass = ((RenameOperationRefactoring) methodRefactoring).getOriginalOperation().getClassName();
        return classClass.equals(methodClass);
    }

    /*
     * Check if the rename class and rename method refactorings can be simplified. If they can, update the rename method
     * refactoring class details.
     */
    public static void checkRenameClassRenameMethodCombination(RefactoringObject renameMethod,
                                                               RefactoringObject renameClass) {
        RenameMethodObject methodObject = (RenameMethodObject) renameMethod;
        String originalMethodClass = methodObject.getOriginalClassName();
        String destinationMethodClass = methodObject.getDestinationClassName();
        RenameClassObject classObject = (RenameClassObject) renameClass;
        String originalClassClass = classObject.getOriginalClassName();
        String destinationClassClass = classObject.getDestinationClassName();

        // If the original class of the rename method refactoring is the same as the original class of the rename class
        // refactoring, update the destination class for the rename method refactoring to be the destination class of
        // the rename class refactoring
        if (originalMethodClass.equals(originalClassClass) && !destinationMethodClass.equals(destinationClassClass)) {
            renameMethod.setDestinationFilePath(classObject.getDestinationFilePath());
            ((RenameMethodObject) renameMethod).setDestinationClassName(classObject.getDestinationClassName());
        }
        // If the destination classes for the rename method and rename class refactorings are the same but the original
        // names are different, then the class was renamed before the method was and we need to update the original
        // class name for the rename method refactoring
        else if (!originalMethodClass.equals(originalClassClass) && destinationMethodClass.equals(destinationClassClass)) {
            renameMethod.setOriginalFilePath(classObject.getOriginalFilePath());
            ((RenameMethodObject) renameMethod).setOriginalClassName(classObject.getOriginalClassName());
        }
        // If the destination class of the rename method is equal to the destination class of the rename class, then the
        // destination of the rename method class needs to be updated to the rename class's destination class name.
        else if (destinationMethodClass.equals(originalClassClass) && !originalMethodClass.equals(originalClassClass)) {
            renameMethod.setDestinationFilePath(classObject.getDestinationFilePath());
            ((RenameMethodObject) renameMethod).setDestinationClassName(classObject.getDestinationClassName());
        }
    }
}
