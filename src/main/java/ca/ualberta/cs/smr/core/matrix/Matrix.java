package ca.ualberta.cs.smr.core.matrix;


import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.HashMap;
import java.util.List;

/*
 * The logic and dispatching for the conflict matrix happens in Matrix.
 */

public class Matrix {
    final Project project;
    static final HashMap<RefactoringType, RefactoringElement> elementMap =
                                                    new HashMap<RefactoringType, RefactoringElement>() {{
       put(RefactoringType.RENAME_METHOD, new RenameMethodElement());
       put(RefactoringType.RENAME_CLASS, new RenameClassElement());
    }};

    static final HashMap<RefactoringType, RefactoringVisitor> visitorMap =
                                                    new HashMap<RefactoringType, RefactoringVisitor>() {{
        put(RefactoringType.RENAME_METHOD, new RenameMethodVisitor());
        put(RefactoringType.RENAME_CLASS, new RenameClassVisitor());
    }};

    public Matrix(Project project) {
        this.project = project;
    }

    /*
     * Iterate through each of the left refactorings to compare against the right refactorings.
     */
    public void runMatrix(List<Refactoring> leftRefactorings, List<Refactoring> rightRefactorings) {
        // Iterates over the refactorings in the left commit
        for (Refactoring leftRefactoring : leftRefactorings) {
            // Compares the refactorings in the right commit against the left refactoring
            compareRefactorings(leftRefactoring, rightRefactorings);
        }
    }

    /*
     * This calls dispatch for each pair of refactorings to check for conflicts.
     */
    void compareRefactorings(Refactoring leftRefactoring, List<Refactoring> rightRefactorings) {
        // Iterate over the right refactorings
        for(Refactoring rightRefactoring : rightRefactorings) {
            // Dispatch the refactoring elements to the correct conflict checker
            dispatch(leftRefactoring, rightRefactoring);
        }

    }

    /*
     * Perform double dispatch to check if the two refactoring elements conflict.
     */
    void dispatch(Refactoring leftRefactoring, Refactoring rightRefactoring) {
        // Get the refactoring types so we can create the corresponding element and visitor
        RefactoringElement element = makeElement(leftRefactoring);
        RefactoringVisitor visitor = makeVisitor(rightRefactoring);
        element.accept(visitor);
    }

    /*
     * Use the refactoring type to get the refactoring element class from the elementMap.
     * Set the refactoring field in the element.
     */
    public RefactoringElement makeElement(Refactoring ref) {
        RefactoringType type = ref.getRefactoringType();
        RefactoringElement element = elementMap.get(type);
        element.set(ref, project);
        return element;
    }

    public RefactoringVisitor makeVisitor(Refactoring ref) {
        RefactoringType type = ref.getRefactoringType();
        RefactoringVisitor visitor = visitorMap.get(type);
        visitor.set(ref);
        return visitor;
    }

}
