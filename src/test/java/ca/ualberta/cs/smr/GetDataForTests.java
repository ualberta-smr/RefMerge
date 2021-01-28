package ca.ualberta.cs.smr;

import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetDataForTests {
        public static List<Refactoring> getRefactorings(String type) {
            String basePath = System.getProperty("user.dir");
            String originalPath = basePath + "/src/test/resources/original";
            String refactoredPath = basePath + "/src/test/resources/refactored";
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

        public static List<Class> getClasses() throws ClassNotFoundException {
            String basePath = System.getProperty("user.dir");
            String originalPath = basePath + "/src/test/resources/original";
            List<Class> classes = new ArrayList<>();
            try {

                UMLModel model1 = new UMLModelASTReader(new File(originalPath)).getUmlModel();
                List<UMLClass> umlClasses = model1.getClassList();
                for(UMLClass umlClass : umlClasses) {
                    classes.add(umlClass.getClass());

                }
                return classes;

            } catch(IOException e) {
                System.out.println("Error: Problem getting refactoring operations");
                e.printStackTrace();
            }
            return classes;
        }

}
