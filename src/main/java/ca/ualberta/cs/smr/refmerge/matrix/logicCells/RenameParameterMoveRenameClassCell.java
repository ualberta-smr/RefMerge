package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameClassObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;

public class RenameParameterMoveRenameClassCell {

    public static void checkCombination(RefactoringObject classObject, RefactoringObject parameterObject) {
        MoveRenameClassObject moveRenameClass = (MoveRenameClassObject) classObject;
        RenameParameterObject parameter = (RenameParameterObject) parameterObject;

        String originalClassName = moveRenameClass.getOriginalClassObject().getPackageName() + "."
                + moveRenameClass.getOriginalClassObject().getClassName();
        String destinationClassName = moveRenameClass.getDestinationClassObject().getPackageName() + "."
                + moveRenameClass.getDestinationClassObject().getClassName();

        String originalParameterClassName = parameter.getOriginalClassName();
        String destinationParameterClassName = parameter.getRefactoredClassName();

        // If the class refactoring happens before the parameter refactoring
        if(destinationClassName.equals(originalParameterClassName)) {
            // Update the original parameter's class
            ((RenameParameterObject) parameterObject).setOriginalClassName(originalClassName);
            parameterObject.setOriginalFilePath(classObject.getOriginalFilePath());
        }

        // If the parameter refactoring happens before the class refactoring
        else if(originalClassName.equals(destinationParameterClassName)) {
            // Update the refactored parameter's class
            ((RenameParameterObject) parameterObject).setRefactoredClassName(destinationClassName);
            parameterObject.setDestinationFilePath(classObject.getDestinationFilePath());
        }
    }

}
