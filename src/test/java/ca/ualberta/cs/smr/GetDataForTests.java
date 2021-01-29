package ca.ualberta.cs.smr;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.UMLModelDiff;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
