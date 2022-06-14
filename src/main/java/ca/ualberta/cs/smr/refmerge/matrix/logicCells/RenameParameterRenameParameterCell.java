package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameParameterObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;

import java.util.List;

public class RenameParameterRenameParameterCell {

    public static boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        // The only conflict that can occur is a naming conflict
        return namingConflict(dispatcher, receiver);
    }

    private static boolean namingConflict(RefactoringObject dispatcher, RefactoringObject receiver) {
        RenameParameterObject dispatcherObject = (RenameParameterObject) dispatcher;
        RenameParameterObject receiverObject = (RenameParameterObject) receiver;

        // Get dispatcher class info
        String dispatcherOriginalClass = dispatcherObject.getOriginalClassName();
        String dispatcherDestinationClass = dispatcherObject.getRefactoredClassName();
        // Get receiver class info
        String receiverOriginalClass = receiverObject.getOriginalClassName();
        String receiverDestinationClass = receiverObject.getRefactoredClassName();

        // Get method signatures
        MethodSignatureObject dispatcherOriginalMethod = dispatcherObject.getOriginalMethodSignature();
        MethodSignatureObject dispatcherDestinationMethod = dispatcherObject.getRefactoredMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiverObject.getOriginalMethodSignature();
        MethodSignatureObject receiverDestinationMethod = receiverObject.getRefactoredMethodSignature();

        // Get parameter objects
        ParameterObject dispatcherOriginalParameter = dispatcherObject.getOriginalParameterObject();
        ParameterObject dispatcherDestinationParameter = dispatcherObject.getRefactoredParameterObject();
        ParameterObject receiverOriginalParameter = receiverObject.getOriginalParameterObject();
        ParameterObject receiverDestinationParameter = receiverObject.getRefactoredParameterObject();

        // If the same parameter is renamed to two different names
        // C1.m1.p1 -> C1.m1.p2 & C1.m1.p1 -> C1.m1.p3
        if(dispatcherOriginalClass.equals(receiverOriginalClass) && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod)) {
            if(dispatcherOriginalParameter.getName().equals(receiverOriginalParameter.getName())
                    && !dispatcherDestinationParameter.getName().equals(receiverDestinationParameter.getName())) {
                return true;
            }
        }
        // If two different parameters are renamed to the same name in the same method
        // C1.m1.p2 -> C1.m1.p1 & C1.m1.p3 -> C1.m1.p1
        if(dispatcherDestinationClass.equals(receiverDestinationClass)) {
            if(dispatcherDestinationParameter.getName().equals(receiverDestinationParameter.getName())
                    && !dispatcherOriginalParameter.getName().equals(receiverOriginalParameter.getName())) {
                if(dispatcherDestinationMethod.getName().equals(receiverDestinationMethod.getName())) {
                    // Check types since names may be different
                    List<ParameterObject> dispatcherParameters = dispatcherDestinationMethod.getParameterList();
                    List<ParameterObject> receiverParameters = receiverDestinationMethod.getParameterList();
                    // Check for overloading
                    if(dispatcherParameters.size() == receiverParameters.size()) {
                        for(int i = 0; i < dispatcherParameters.size(); i++) {
                            String dispatcherType = dispatcherParameters.get(i).getType();
                            String receiverType = receiverParameters.get(i).getType();
                            if(!dispatcherType.equals(receiverType)) {
                                return false;
                            }
                        }
                        return true;
                    }

                }
            }
        }

        return false;
    }

}
