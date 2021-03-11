package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;

import java.util.List;

import static org.mockito.Mockito.times;

public class RenameMethodElementTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    public void testAccept() {
        RenameMethodElement element = Mockito.mock(RenameMethodElement.class);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    public void testSet() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        Node node = new Node(ref);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        Assert.assertNotNull("The refactoring element should not be null", element.elementRef);

    }

    public void testCheckRenameMethodOverrideConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverrideConflict/refactored";
        String configurePath = "renameMethodRenameMethodFiles/methodOverrideConflict/original/Override.java";
        myFixture.configureByFiles(configurePath);
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 5;
        Refactoring renameParentFooMethod = refactorings.get(0);
        Node node = new Node(renameParentFooMethod);
        Refactoring renameChildBarMethod = refactorings.get(2);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        boolean isConflicting = element.checkRenameMethodConflict(renameChildBarMethod);
        Assert.assertTrue("Originally overriding methods that are renamed to different names conflict", isConflicting);
    }

    public void testCheckRenameMethodOverloadConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodOverloadConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        assert refactorings.size() == 4;
        Refactoring changeFirstOverloaded = refactorings.get(0);
        Node node = new Node(changeFirstOverloaded);
        Refactoring changeSecondOverloaded = refactorings.get(1);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        boolean isConflicting = element.checkRenameMethodConflict(changeSecondOverloaded);
        Assert.assertTrue("Methods that start overloaded and get changed to different names should conflict", isConflicting);
    }

    public void testCheckRenameMethodNamingConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(0);
        Node node = new Node(elementRef);
        Refactoring visitorRef = refactorings.get(1);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        boolean isConflicting = element.checkRenameMethodConflict(visitorRef);
        Assert.assertTrue("Methods renamed to the same name in the same class should return true", isConflicting);
    }

    public void testCheckRenameMethodNoConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameMethodRenameMethodFiles/methodNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring elementRef = refactorings.get(1);
        Node node = new Node(elementRef);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
        boolean isConflicting = element.checkRenameMethodConflict(elementRef);
        Assert.assertFalse("A method renamed to the same name in both versions should not conflict", isConflicting);
    }

    public void testCheckRenameMethodDependence() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/original";
        String refactoredPath = basePath + "/src/test/testData/renameMethodRenameClassFiles/dependence/refactored";
        List<Refactoring> methodRefs = GetDataForTests.getRefactorings("RENAME_METHOD", originalPath, refactoredPath);
        List<Refactoring> classRefs = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert classRefs != null;
        Refactoring visitorRef = classRefs.get(0);
        assert methodRefs != null;
        Refactoring elementRef = methodRefs.get(0);
        Node node = new Node(elementRef);
        RenameMethodElement element = new RenameMethodElement();
        element.set(node, project);
//        boolean isDependent = element.checkRenameClassDependence(visitorRef);
//        Assert.assertTrue(isDependent);
        Refactoring r = element.checkRenameClassDependence(visitorRef);
        Assert.assertNotNull(r);
    }
}
