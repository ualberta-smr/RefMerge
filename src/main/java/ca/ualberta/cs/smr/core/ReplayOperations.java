package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.Set;


public class ReplayOperations {

    Project project;

    public ReplayOperations(Project proj) {
        this.project = proj;
    }

    /*
     * replayRenameMethod performs the rename method refactoring.
     */
    public void replayRenameMethod(Refactoring ref) {
        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        String destName = renamed.getName();
        String qualifiedClass = renamed.getClassName();
        String filePath = renamed.getLocationInfo().getFilePath();
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(qualifiedClass, filePath);
        assert psiClass != null;
        PsiMethod method = Utils.getPsiMethod(psiClass, original);
        assert method != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(method, destName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
        // Update the virtual file containing the refactoring
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);
    }


    public void replayRenameClass(Refactoring ref) {

        UMLClass original = ((RenameClassRefactoring) ref).getOriginalClass();
        UMLClass renamed = ((RenameClassRefactoring) ref).getRenamedClass();
        String srcQualifiedClass = original.getName();
        String destQualifiedClass = renamed.getName();
        String destClassName = destQualifiedClass.substring(destQualifiedClass.lastIndexOf(".") + 1);
        Utils utils = new Utils(project);
        String filePath = original.getLocationInfo().getFilePath();
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(srcQualifiedClass, filePath);
        assert psiClass != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(psiClass, destClassName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

    public void replayExtractMethod(Refactoring ref) throws PrepareFailedException {

        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
        UMLOperation sourceOperation = extractOperationRefactoring.getSourceOperationBeforeExtraction();
        UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();
        String refactoringName = extractedOperation.getName();
        String initialMethodName = sourceOperation.getName();
        String initialClassName = sourceOperation.getClassName();
        String filePath = sourceOperation.getLocationInfo().getFilePath();
        String helpId = "";

        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(initialClassName, filePath);
        assert psiClass != null;
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, sourceOperation);
        assert psiMethod != null;
        PsiElement[] psiElements = getPsiElements(extractOperationRefactoring, psiMethod);
        PsiType forcedReturnType = getPsiReturnType(extractOperationRefactoring, psiMethod);

        ExtractMethodProcessor extractMethodProcessor = new ExtractMethodProcessor(project, null, psiElements,
                forcedReturnType, refactoringName, initialMethodName, helpId);
        extractMethodProcessor.setMethodName(refactoringName);
        extractMethodProcessor.prepare();
        extractMethodProcessor.setDataFromInputVariables();
        ExtractMethodHandler.extractMethod(project, extractMethodProcessor);
    }

    private PsiElement[] getPsiElements(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        ArrayList<PsiElement> psiElements = new ArrayList<>();
        Set<AbstractCodeFragment> codeFragments = extractOperationRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;
        for(AbstractCodeFragment codeFragment : codeFragments) {
            int startOffset = codeFragment.getLocationInfo().getStartOffset();
            int endOffset = codeFragment.getLocationInfo().getEndOffset();
            ASTNode node = SourceTreeToPsiMap.psiElementToTree(psiMethod);
            assert node != null;
            ArrayList<ASTNode> nodes = getMethodNodes(node, startOffset, endOffset);
            for(ASTNode astNode : nodes) {
                psiElements.add(astNode.getPsi());
            }
        }
        return psiElements.toArray(new PsiElement[0]);
    }

    private ArrayList<ASTNode> getMethodNodes(ASTNode node, int start, int end) {
        ArrayList<ASTNode> astNodes = new ArrayList<>();
        ASTNode[] children = node.getChildren(null);
        if(children.length < 1) {
            return astNodes;
        }
        int nodeStart = node.getStartOffset();
        int nodeEnd = node.getTextRange().getEndOffset();
        if (nodeStart >= start && nodeEnd <= end) {
            PsiElement psiElement = node.getPsi();
            if(!(psiElement instanceof  PsiJavaToken) && !(psiElement instanceof  PsiReferenceParameterList)
                    && !(psiElement instanceof PsiReferenceExpression) && !(psiElement instanceof PsiBinaryExpression)
                    && !(psiElement instanceof  PsiExpressionList) && !(psiElement instanceof PsiMethodCallExpression)) {
                astNodes.add(node);
            }
        }

        for(ASTNode child : children) {
            astNodes.addAll(getMethodNodes(child, start, end));
        }

        return astNodes;
    }

    private PsiType getPsiReturnType(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        UMLParameter returnParameter = extractOperationRefactoring.getExtractedOperation().getReturnParameter();
        String parameterType = returnParameter.getType().toString();
        PsiElementFactory factory = PsiElementFactory.getInstance(project);
        if(parameterType.equals("void")) {
            return factory.createTypeFromText(parameterType, psiMethod);
        }
        String parameterName = returnParameter.getName();
        String parameter = parameterType + " " + parameterName;
        return factory.createTypeFromText(parameter, psiMethod);
    }
}
