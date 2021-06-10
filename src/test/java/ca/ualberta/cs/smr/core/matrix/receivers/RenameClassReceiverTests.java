package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class RenameClassReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        RenameClassReceiver receiver = new RenameClassReceiver();
        receiver.set(refactoringObject, null);
        Assert.assertNotNull("The refactoring element should not be null", receiver.refactoringObject);
    }

    public void testRenameMethodDispatcherReceive() {
        RenameClassDispatcher dispatcher = new RenameClassDispatcher();
        MoveRenameMethodDispatcher wrongDispatcher = new MoveRenameMethodDispatcher();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);

    }

    public void testRenameClassDispatcherReceive() {
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        RenameClassDispatcher wrongDispatcher = new RenameClassDispatcher();
        RenameClassReceiver receiver = mock(RenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);
    }

    public void testRenameClassRenameClassConflictCell() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(foo);
        RefactoringObject rightRefactoring = RefactoringObjectUtils.createRefactoringObject(foo2);
        boolean isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue(isConflicting);
        rightRefactoring = RefactoringObjectUtils.createRefactoringObject(bar);
        isConflicting = RenameClassRenameClassCell.renameClassRenameClassConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertFalse(isConflicting);
    }

    public void testRenameClassRenameMethodDependenceCell() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert methodRefs != null;
        Refactoring leftRef = methodRefs.get(0);
        assert classRefs != null;
        Refactoring rightRef = classRefs.get(0);
        RefactoringObject leftRefactoring = RefactoringObjectUtils.createRefactoringObject(leftRef);
        RefactoringObject rightRefactoring = RefactoringObjectUtils.createRefactoringObject(rightRef);
        boolean isDependent = RenameClassMoveRenameMethodCell.renameClassRenameMethodDependenceCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue(isDependent);
    }
}
