package ca.ualberta.cs.smr.testUtils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import gr.uom.java.xmi.UMLClass;

import java.util.ArrayList;
import java.util.Arrays;
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
        }
        return names;

    }

    public static PsiMethod[] getPsiMethodsFromFile(PsiFile psiFile) {
        ArrayList<PsiMethod> psiMethods = new ArrayList<>();
        PsiClass[] psiClasses = getPsiClassesFromFile(psiFile);
        for(PsiClass psiClass : psiClasses) {
            PsiMethod[] methods = psiClass.getMethods();
            psiMethods.addAll(Arrays.asList(methods));
        }
        PsiMethod[] methodArray = new PsiMethod[psiMethods.size()];
        for(int i = 0; i < psiMethods.size(); i++) {
            methodArray[i] = psiMethods.get(i);
        }
        return methodArray;
    }

    public static PsiClass[] getPsiClassesFromFile(PsiFile psiFile) {
        ArrayList<PsiClass> psiClasses = new ArrayList<>();
        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        PsiClass[] classes = psiJavaFile.getClasses();
        for (PsiClass psiClass : classes) {
            psiClasses.add(psiClass);
            PsiClass[] innerClasses = psiClass.getInnerClasses();
            psiClasses.addAll(Arrays.asList(innerClasses));
        }
        PsiClass[] classArray = new PsiClass[psiClasses.size()];
        for(int i = 0; i < psiClasses.size(); i++) {
            classArray[i] = psiClasses.get(i);
        }
        return classArray;
    }

    public static PsiClass findPsiClassFromUML(UMLClass umlClass, PsiClass[] psiClasses) {
        for(PsiClass psiClass : psiClasses) {
            if(Objects.equals(psiClass.getName(), umlClass.getName())) {
                return psiClass;
            }
        }
        return null;
    }
}
