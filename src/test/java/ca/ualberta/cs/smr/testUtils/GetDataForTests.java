package ca.ualberta.cs.smr.testUtils;


import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.CodeRange;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;
import org.refactoringminer.api.RefactoringType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetDataForTests {
    public static List<Refactoring> getRefactorings(String type, String originalPath, String refactoredPath) {

        List<Refactoring> refs = new ArrayList<>();
        try {

            UMLModel model1 = new UMLModelASTReader(new File(originalPath)).getUmlModel();
            UMLModel model2 = new UMLModelASTReader(new File(refactoredPath)).getUmlModel();
            UMLModelDiff modelDiff = model1.diff(model2);
            List<Refactoring> refactorings = modelDiff.getRefactorings();
            if(refactorings == null) {
                return null;
            }
            for(Refactoring ref : refactorings) {
                if(ref.getRefactoringType().toString().equals(type)) {
                    refs.add(ref);
                }
            }
            return refs;
        } catch(IOException | RefactoringMinerTimedOutException e) {
            System.out.println("Error: Problem getting refactoring operations");
            e.printStackTrace();
        }
        return null;
    }

    public static List<RefactoringObject> getRefactoringObjects(String type, String originalPath, String refactoredPath) {

        List<RefactoringObject> refs = new ArrayList<>();
        try {

            UMLModel model1 = new UMLModelASTReader(new File(originalPath)).getUmlModel();
            UMLModel model2 = new UMLModelASTReader(new File(refactoredPath)).getUmlModel();
            UMLModelDiff modelDiff = model1.diff(model2);
            List<Refactoring> refactorings = modelDiff.getRefactorings();
            if(refactorings == null) {
                return null;
            }
            for(Refactoring ref : refactorings) {
                if(ref.getRefactoringType().toString().equals(type)) {
                    RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(ref);
                    refs.add(refactoringObject);
                }
            }
            return refs;
        } catch(IOException | RefactoringMinerTimedOutException e) {
            System.out.println("Error: Problem getting refactoring operations");
            e.printStackTrace();
        }
        return null;
    }

    public static UMLClass getClass(String path, String className) {

        try {
            UMLModel model = new UMLModelASTReader(new File(path)).getUmlModel();
            List<UMLClass> umlClasses = model.getClassList();
            for(UMLClass umlClass : umlClasses) {
                if(umlClass.getName().equals(className)) {
                    return umlClass;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Refactoring getEmptyRefactoring(RefactoringType type) {
        return new EmptyRefactoring(type);
    }

}

class EmptyRefactoring implements Refactoring {
    RefactoringType type;

    public EmptyRefactoring(RefactoringType type) {
        this.type = type;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return type;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
        return null;
    }

    @Override
    public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
        return null;
    }

    @Override
    public List<CodeRange> leftSide() {
        return null;
    }

    @Override
    public List<CodeRange> rightSide() {
        return null;
    }
}
