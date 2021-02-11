package ca.ualberta.cs.smr.testUtils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtils {
    public static List<String> getMethodNames(PsiMethod[] methods) {
        ArrayList<String> names = new ArrayList<>();
        for (PsiMethod method : methods) {
            names.add(method.getName());
        }
        return names;
    }

    public static List<String> getClassNames(PsiClass[] classes) {
        ArrayList<String> names = new ArrayList<>();
        for (PsiClass psiClass : classes) {
            names.add(psiClass.getName());
            PsiClass[] innerClasses = psiClass.getInnerClasses();
            for(PsiClass innerClass : innerClasses) {
                names.add(innerClass.getName());
            }
        }
        return names;

    }

    public static PsiMethod[] getPsiMethodsFromFile(PsiFile psiFile) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = psiJavaFile.getClasses()[0];
        return psiClass.getMethods();
    }

    public static PsiClass[] getPsiClassesFromFile(PsiFile psiFile) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        return psiJavaFile.getClasses();
    }
}
