package ca.ualberta.cs.smr.testUtils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static List<String> getMethodNames(PsiMethod[] methods) {
        ArrayList<String> names = new ArrayList<>();
        for (PsiMethod method : methods) {
            names.add(method.getName());
        }
        return names;
    }

    public static PsiMethod[] getPsiMethodsFromFile(PsiFile psiFile) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass psiClass = psiJavaFile.getClasses()[0];
        return psiClass.getMethods();
    }
}
