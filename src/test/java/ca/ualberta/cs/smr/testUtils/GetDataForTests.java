package ca.ualberta.cs.smr.testUtils;


import ca.ualberta.cs.smr.utils.sortingUtils.Pair;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Pair> getPairs(String type, String originalPath, String refactoredPath) {

        List<Pair> refs = new ArrayList<>();
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
                    Pair pair = new Pair(0, ref);
                    refs.add(pair);
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

}
