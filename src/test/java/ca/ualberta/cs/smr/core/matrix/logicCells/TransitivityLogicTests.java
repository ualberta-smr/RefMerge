package ca.ualberta.cs.smr.core.matrix.logicCells;

import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.core.refactoringObjects.RenameMethodObject;
import org.junit.Assert;
import org.junit.Test;

public class TransitivityLogicTests {

    @Test
    public void testFoundRenameMethodRenameMethodTransitivity() {
        // Rename method A.foo -> A.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject();
        firstRefactoring.setOriginalFilePath("A.java");
        firstRefactoring.setOriginalClassName("A");
        firstRefactoring.setOriginalMethodName("foo");
        firstRefactoring.setDestinationFilePath("A.java");
        firstRefactoring.setDestinationClassName("A");
        firstRefactoring.setDestinationMethodName("bar");
        // Rename method A.bar -> A.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject();
        secondRefactoring.setOriginalFilePath("A.java");
        secondRefactoring.setOriginalClassName("A");
        secondRefactoring.setOriginalMethodName("bar");
        secondRefactoring.setDestinationFilePath("A.java");
        secondRefactoring.setDestinationClassName("A");
        secondRefactoring.setDestinationMethodName("foobar");
        // Rename Method A.foo -> A.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setOriginalMethodName("foo");
        expectedRefactoring.setDestinationFilePath("A.java");
        expectedRefactoring.setDestinationClassName("A");
        expectedRefactoring.setDestinationMethodName("foobar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    @Test
    public void testFoundRenameMethodRenameMethodTransitivity2() {
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject();
        firstRefactoring.setOriginalFilePath("A.java");
        firstRefactoring.setOriginalClassName("A");
        firstRefactoring.setOriginalMethodName("foo");
        firstRefactoring.setDestinationFilePath("B.java");
        firstRefactoring.setDestinationClassName("B");
        firstRefactoring.setDestinationMethodName("bar");
        // Rename method B.bar -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject();
        secondRefactoring.setOriginalFilePath("B.java");
        secondRefactoring.setOriginalClassName("B");
        secondRefactoring.setOriginalMethodName("bar");
        secondRefactoring.setDestinationFilePath("C.java");
        secondRefactoring.setDestinationClassName("C");
        secondRefactoring.setDestinationMethodName("foobar");
        // Rename Method A.foo -> C.foobar
        RenameMethodObject expectedRefactoring = new RenameMethodObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setOriginalMethodName("foo");
        expectedRefactoring.setDestinationFilePath("C.java");
        expectedRefactoring.setDestinationClassName("C");
        expectedRefactoring.setDestinationMethodName("foobar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,true);
    }

    @Test
    public void testNotFoundRenameMethodRenameMethodTransitivity() {
        // Rename method A.foo -> B.bar
        RenameMethodObject firstRefactoring = new RenameMethodObject();
        firstRefactoring.setOriginalFilePath("A.java");
        firstRefactoring.setOriginalClassName("A");
        firstRefactoring.setOriginalMethodName("foo");
        firstRefactoring.setDestinationFilePath("B.java");
        firstRefactoring.setDestinationClassName("B");
        firstRefactoring.setDestinationMethodName("bar");
        // Rename method B.buzz -> C.foobar
        RenameMethodObject secondRefactoring = new RenameMethodObject();
        secondRefactoring.setOriginalFilePath("B.java");
        secondRefactoring.setOriginalClassName("B");
        secondRefactoring.setOriginalMethodName("buzz");
        secondRefactoring.setDestinationFilePath("C.java");
        secondRefactoring.setDestinationClassName("C");
        secondRefactoring.setDestinationMethodName("foobar");
        // Rename Method A.foo -> B.bar
        RenameMethodObject expectedRefactoring = new RenameMethodObject();
        expectedRefactoring.setOriginalFilePath("A.java");
        expectedRefactoring.setOriginalClassName("A");
        expectedRefactoring.setOriginalMethodName("foo");
        expectedRefactoring.setDestinationFilePath("B.java");
        expectedRefactoring.setDestinationClassName("B");
        expectedRefactoring.setDestinationMethodName("bar");

        doRenameMethodRenameMethodTest(firstRefactoring, secondRefactoring, expectedRefactoring,false);
    }

    private void doRenameMethodRenameMethodTest(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring,
                                                RefactoringObject expectedRefactoring, boolean expectedTransitivity) {
        RenameMethodRenameMethodCell cell = new RenameMethodRenameMethodCell(null);
        boolean isTransitive = cell.checkRenameMethodRenameMethodTransitivity(firstRefactoring, secondRefactoring);
        if(expectedTransitivity) {
            Assert.assertTrue(isTransitive);
            Assert.assertEquals(expectedRefactoring.getDestinationFilePath(), firstRefactoring.getDestinationFilePath());
            Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationMethodName(),
                    ((RenameMethodObject) firstRefactoring).getDestinationMethodName());
            Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationClassName(),
                    ((RenameMethodObject) firstRefactoring).getDestinationClassName());
            Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getOriginalMethodName(),
                    ((RenameMethodObject) firstRefactoring).getOriginalMethodName());
            Assert.assertNotEquals(((RenameMethodObject) expectedRefactoring).getOriginalMethodName(),
                    ((RenameMethodObject) secondRefactoring).getOriginalMethodName());
        }
        else {
            Assert.assertFalse(isTransitive);
            Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getOriginalMethodName(),
                    ((RenameMethodObject) firstRefactoring).getOriginalMethodName());
            Assert.assertEquals(((RenameMethodObject) expectedRefactoring).getDestinationMethodName(),
                    ((RenameMethodObject) firstRefactoring).getDestinationMethodName());
        }
    }

}
