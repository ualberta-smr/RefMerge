package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.elements.RefactoringElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameClassElement;
import ca.ualberta.cs.smr.core.matrix.elements.RenameMethodElement;
import ca.ualberta.cs.smr.core.matrix.visitors.RefactoringVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameClassVisitor;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MatrixTest extends LightJavaCodeInsightFixtureTestCase {

    @Test
    public void testElementMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassElement renameClassElement = new RenameClassElement();
        RenameMethodElement renameMethodElement = new RenameMethodElement();
        RefactoringElement element = Matrix.elementMap.get(type);
        boolean equals = element.getClass().equals(renameClassElement.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        element = Matrix.elementMap.get(type);
        equals = element.getClass().equals(renameMethodElement.getClass());
        Assert.assertTrue(equals);
    }

    @Test
    public void testVisitorMap() {
        RefactoringType type = RefactoringType.RENAME_CLASS;
        RenameClassVisitor renameClassVisitor = new RenameClassVisitor();
        RenameMethodVisitor renameMethodVisitor = new RenameMethodVisitor();
        RefactoringVisitor visitor = Matrix.visitorMap.get(type);
        boolean equals = visitor.getClass().equals(renameClassVisitor.getClass());
        Assert.assertTrue(equals);
        type = RefactoringType.RENAME_METHOD;
        visitor = Matrix.visitorMap.get(type);
        equals = visitor.getClass().equals(renameMethodVisitor.getClass());
        Assert.assertTrue(equals);
    }

    @Test
    public void testMakeElement() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0).getValue();
        RenameMethodElement mockElement = new RenameMethodElement();
        Matrix matrix = new Matrix(null);
        RefactoringElement element = matrix.makeElement(ref);
        boolean equals = element.getClass().equals(mockElement.getClass());
        Assert.assertTrue(equals);


    }

    @Test
    public void testMakeVisitor() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameMethodVisitor mockVisitor = new RenameMethodVisitor();
        Matrix matrix = new Matrix(null);
        RefactoringVisitor visitor = matrix.makeVisitor(ref);
        boolean equals = visitor.getClass().equals(mockVisitor.getClass());
        Assert.assertTrue(equals);
    }


    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void testDispatch() {
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Pair> refactorings = GetDataForTests.getPairs("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1).getValue();
        Refactoring visitorRef = refactorings.get(2).getValue();
        Matrix matrix = new Matrix(null);
        matrix.dispatch(elementRef, visitorRef);
        String message = "Overload conflict\n" + "Rename Method/Rename Method conflict: true\n";
        Assert.assertEquals(message, outContent.toString());

    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testCompareRefactorings() {
        Matrix matrix = Mockito.mock(Matrix.class);
        Refactoring ref = Mockito.mock(Refactoring.class);
        List<Pair> refList = new ArrayList<>();
        refList.add(new Pair(0,ref));
        Mockito.doCallRealMethod().when(matrix).compareRefactorings(ref, refList);
        matrix.compareRefactorings(ref, refList);
        Mockito.verify(matrix, Mockito.times(1)).dispatch(ref, ref);

    }

    @Test
    public void testRunMatrix() {
        Matrix matrix = Mockito.mock(Matrix.class);
        Refactoring ref = Mockito.mock(Refactoring.class);
        List<Pair> refList = new ArrayList<>();
        refList.add(new Pair(0,ref));
        Mockito.doCallRealMethod().when(matrix).runMatrix(refList, refList);
        matrix.runMatrix(refList, refList);
        Mockito.verify(matrix, Mockito.times(1)).compareRefactorings(ref, refList);

    }
}
