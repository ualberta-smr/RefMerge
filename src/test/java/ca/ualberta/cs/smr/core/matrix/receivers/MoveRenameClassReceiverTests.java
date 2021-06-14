package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.MoveRenameClassMoveRenameMethodCell;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.*;

public class MoveRenameClassReceiverTests extends LightJavaCodeInsightFixtureTestCase {

    public void testSet() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
        MoveRenameClassReceiver receiver = new MoveRenameClassReceiver();
        receiver.set(refactoringObject, null);
        Assert.assertNotNull("The refactoring element should not be null", receiver.refactoringObject);
    }

    public void testMoveRenameMethodDispatcherReceive() {
        MoveRenameClassDispatcher dispatcher = new MoveRenameClassDispatcher();
        MoveRenameMethodDispatcher wrongDispatcher = new MoveRenameMethodDispatcher();
        MoveRenameClassReceiver receiver = mock(MoveRenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);

    }

    public void testMoveRenameClassDispatcherReceive() {
        MoveRenameMethodDispatcher dispatcher = new MoveRenameMethodDispatcher();
        MoveRenameClassDispatcher wrongDispatcher = new MoveRenameClassDispatcher();
        MoveRenameClassReceiver receiver = mock(MoveRenameClassReceiver.class);
        dispatcher.dispatch(receiver);
        verify(receiver, times(1)).receive(dispatcher);
        verify(receiver, never()).receive(wrongDispatcher);
    }

    public void testMoveRenameClassMoveRenameClassConflictCell() {
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
        boolean isConflicting = MoveRenameClassMoveRenameClassCell.MoveRenameClassMoveRenameClassConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue(isConflicting);
        rightRefactoring = RefactoringObjectUtils.createRefactoringObject(bar);
        isConflicting = MoveRenameClassMoveRenameClassCell.MoveRenameClassMoveRenameClassConflictCell(leftRefactoring, rightRefactoring);
        Assert.assertFalse(isConflicting);
    }

    public void testMoveRenameClassRenameMethodDependenceCell() {
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
        boolean isDependent = MoveRenameClassMoveRenameMethodCell.moveRenameClassMoveRenameMethodDependenceCell(leftRefactoring, rightRefactoring);
        Assert.assertTrue(isDependent);
    }
}
