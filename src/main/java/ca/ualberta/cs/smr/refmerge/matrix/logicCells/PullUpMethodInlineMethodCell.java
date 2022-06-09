package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.InlineMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.PullUpMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.project.Project;

public class PullUpMethodInlineMethodCell {

    Project project;

    public PullUpMethodInlineMethodCell(Project project) {
        this.project = project;
    }

    public boolean conflictCell(RefactoringObject dispatcher, RefactoringObject receiver) {
        InlineMethodObject inlineMethodObject = (InlineMethodObject) dispatcher;
        PullUpMethodObject pullUpMethodObject = (PullUpMethodObject) receiver;

        // Naming conflict check
        return namingConflict(inlineMethodObject, pullUpMethodObject);
    }

    public boolean namingConflict(InlineMethodObject dispatcher, PullUpMethodObject receiver) {
        String dispatcherOriginalClass = dispatcher.getOriginalClassName();
        String receiverOriginalClass = receiver.getOriginalClass();

        // Only need to get the original signatures because the inline method refactoring destroys the program element when
        // the refactoring is performed.
        MethodSignatureObject dispatcherOriginalMethod = dispatcher.getOriginalMethodSignature();
        MethodSignatureObject receiverOriginalMethod = receiver.getOriginalMethodSignature();


        // If two methods are pulled up from and inlined from the same method, it is conflicting
        return dispatcherOriginalClass.equals(receiverOriginalClass)
                && dispatcherOriginalMethod.equalsSignature(receiverOriginalMethod);
    }

}
