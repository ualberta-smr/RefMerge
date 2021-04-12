package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.core.refactoringWrappers.ExtractOperationRefactoringWrapper;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.refactoring.changeSignature.ChangeSignatureProcessor;
import com.intellij.refactoring.changeSignature.ParameterInfoImpl;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.decomposition.*;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
import java.util.List;
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

    public void replayExtractMethod(Refactoring ref) {
        ExtractOperationRefactoringWrapper refactoringWrapper = (ExtractOperationRefactoringWrapper) ref;
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

        PsiElement[] psiElements;
        PsiStatement[] surroundingStatements = refactoringWrapper.getSurroundingStatements();

        if(surroundingStatements == null) {
            psiElements = getPsiElements(extractOperationRefactoring, psiMethod);
        }
        else {
            psiElements = getPsiElementsBetweenStatements(surroundingStatements, psiMethod);
        }
        PsiType forcedReturnType = getPsiReturnType(extractOperationRefactoring, psiMethod);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        // Set editor to null because we are not using the character offset in the editor
        ExtractMethodProcessor extractMethodProcessor = new ExtractMethodProcessor(project, editor, psiElements,
                forcedReturnType, refactoringName, initialMethodName, helpId);
        extractMethodProcessor.setMethodName(refactoringName);
        String visibility = extractedOperation.getVisibility();
        extractMethodProcessor.setMethodVisibility(visibility);
        try {
            extractMethodProcessor.prepare();
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }
        extractMethodProcessor.setDataFromInputVariables();
        ExtractMethodHandler.extractMethod(project, extractMethodProcessor);
        PsiMethod extractedPsiMethod = extractMethodProcessor.getExtractedMethod();
        if(extractedPsiMethod.getParameterList().getParametersCount() > 1) {
            ParameterInfoImpl[] parameterInfo = getParameterInfo(extractedPsiMethod, extractedOperation);
            ChangeSignatureProcessor changeSignatureProcessor =
                    new ChangeSignatureProcessor(project, extractedPsiMethod, false, null,
                            refactoringName, forcedReturnType, parameterInfo);
            changeSignatureProcessor.run();
        }
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

    /*
     * Use the new psi statement at the beginning and end of the extracted method to get all involved psi statements
     * in the refactoring.
     */
    private PsiElement[] getPsiElementsBetweenStatements(PsiStatement[] surroundingStatements, PsiMethod psiMethod) {
        PsiStatement firstStatement = surroundingStatements[0];
        PsiStatement lastStatement = surroundingStatements[1];
        String startingText = formatText(firstStatement.getText());
        String endingText = formatText(lastStatement.getText());

        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;
        PsiStatement[] psiStatements = psiCodeBlock.getStatements();

        ArrayList<PsiElement> psiElements = getPsiElementsFromStatements(psiStatements, startingText, endingText);
        return psiElements.toArray(new PsiElement[0]);

    }

    /*
     * Use the code fragments to get the code fragments if the beginning and ending line of the extracted method are the
     * same.
     */
    private PsiElement[] getPsiElements(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        Set<AbstractCodeFragment> codeFragments = extractOperationRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        AbstractCodeFragment[] codeFragmentsArray = new AbstractCodeFragment[codeFragments.size()];
        codeFragments.toArray(codeFragmentsArray);
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;
        PsiStatement[] psiStatements = psiCodeBlock.getStatements();
        AbstractCodeFragment firstCodeFragment = codeFragmentsArray[0];
        AbstractCodeFragment lastCodeFragment = codeFragmentsArray[codeFragmentsArray.length - 1];
        String startingText = formatText(firstCodeFragment.getString());
        String endingText = formatText(lastCodeFragment.getString());
        ArrayList<PsiElement> psiElements = getPsiElementsFromStatements(psiStatements, startingText, endingText);
        return psiElements.toArray(new PsiElement[0]);
    }

    /*
     * Format the text to remove new lines and spaces for comparing code fragments
     */
    private String formatText(String text) {
        text = text.replaceAll(" ", "");
        text = text.replaceAll("\n", "");
        return text;
    }

    /*
     * Gets the PSI elements for the extract method processor. If the surrounding statements are not null, use them to
     * get the PSI elements. If they are null, use the code fragments instead.
     */
    private ArrayList<PsiElement> getPsiElementsFromStatements(PsiStatement[] psiStatements,
                                                               String startingText,
                                                               String endingText) {

        ArrayList<PsiElement> psiElements = new ArrayList<>();
        boolean statementInRange = false;

        for(PsiStatement psiStatement : psiStatements) {
            String psiStatementText = formatText(psiStatement.getText());
            if(startingText.equals(psiStatementText)) {
                statementInRange = true;
            }
            if(statementInRange) {
                ASTNode node = SourceTreeToPsiMap.psiElementToTree(psiStatement);
                assert node != null;
                psiElements.add(node.getPsi());
            }
            if(endingText.equals(psiStatementText)) {
                break;
            }
        }
        return psiElements;

    }

    /*
     * Gets the return type of the extracted method.
     */
    private PsiType getPsiReturnType(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        UMLParameter returnParameter = extractOperationRefactoring.getExtractedOperation().getReturnParameter();
        String parameterType = returnParameter.getType().toString();
        PsiElementFactory factory = PsiElementFactory.getInstance(project);
        return factory.createTypeFromText(parameterType, psiMethod);
    }

    /*
     * Use the PSI method and UML operation to reorder the parameters in the extracted method.
     */
    private ParameterInfoImpl[] getParameterInfo(PsiMethod psiMethod, UMLOperation umlOperation) {

        List<UMLParameter> umlParameterList = umlOperation.getParameters();
        PsiParameterList psiParameterList = psiMethod.getParameterList();
        ParameterInfoImpl[] parameterInfoImplArray = new ParameterInfoImpl[umlParameterList.size() - 1];
        for(int i = 1; i < umlParameterList.size(); i++) {
            UMLParameter umlParameter = umlParameterList.get(i);
            String umlParameterType = umlParameter.getType().toString();
            String umlParameterName = umlParameter.getName();
            ParameterInfoImpl parameterInfo;
            for(PsiParameter psiParameter : psiParameterList.getParameters()) {
                String psiParameterName = psiParameter.getName();
                PsiType psiType = psiParameter.getType();
                String psiParameterType = psiType.getPresentableText();
                if(umlParameterName.equals(psiParameterName) && umlParameterType.equals(psiParameterType)) {
                    int index = psiParameterList.getParameterIndex(psiParameter);
                    parameterInfo = ParameterInfoImpl.create(index).withName(psiParameterName).withType(psiType);
                    parameterInfoImplArray[i-1] = parameterInfo;
                    break;
                }
            }

        }



        return parameterInfoImplArray;

    }

}
