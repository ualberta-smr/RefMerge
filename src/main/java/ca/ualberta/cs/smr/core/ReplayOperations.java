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

        // Set editor to null because we are not using the character offset in the editor
        ExtractMethodProcessor extractMethodProcessor = new ExtractMethodProcessor(project, null, psiElements,
                forcedReturnType, refactoringName, initialMethodName, helpId);
        extractMethodProcessor.setMethodName(refactoringName);
        try {
            extractMethodProcessor.prepare();
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }
        extractMethodProcessor.setDataFromInputVariables();
        ExtractMethodHandler.extractMethod(project, extractMethodProcessor);

        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

    private PsiElement[] getPsiElements(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        Set<AbstractCodeFragment> codeFragments = extractOperationRefactoring.getExtractedCodeFragmentsFromSourceOperation();
        AbstractCodeFragment[] codeFragmentsArray = new AbstractCodeFragment[codeFragments.size()];
        codeFragments.toArray(codeFragmentsArray);
        AbstractStatement[] statements = getSurroundingStatements(codeFragmentsArray[0],
                                                            codeFragmentsArray[codeFragmentsArray.length - 1]);
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;
        PsiStatement[] psiStatements = psiCodeBlock.getStatements();
        AbstractCodeFragment firstCodeFragment = codeFragmentsArray[0];
        AbstractCodeFragment lastCodeFragment = codeFragmentsArray[codeFragmentsArray.length - 1];
        ArrayList<PsiElement> psiElements = getPsiElementsFromStatements(psiStatements, firstCodeFragment, lastCodeFragment,
                                                            statements);
        return psiElements.toArray(new PsiElement[0]);
    }

    private String formatText(String text) {
        text = text.replaceAll(" ", "");
        text = text.replaceAll("\n", "");
        return text;
    }

    /*
     * Gets the statement before and after the statements that are being extracted to the new method. If the first code
     * fragment is the first statement, set the first statement to null and if the last code fragment is the last statement,
     * set the last statement to null.
     */
    private AbstractStatement[] getSurroundingStatements(AbstractCodeFragment firstCodeFragment,
                                                         AbstractCodeFragment lastCodeFragment) {
        CompositeStatementObject compositeStatementObject = firstCodeFragment.getParent();
        List<AbstractStatement> abstractStatements = compositeStatementObject.getStatements();
        AbstractStatement[] surroundingStatements = new AbstractStatement[2];
        if(firstCodeFragment.equalFragment(lastCodeFragment)) {
            surroundingStatements[0] = null;
            surroundingStatements[1] = null;
            return surroundingStatements;
        }
        for(int i = 0; i < abstractStatements.size(); i++) {
            AbstractStatement abstractStatement = abstractStatements.get(i);
            if(abstractStatement.equalFragment(firstCodeFragment)) {
                if(i != 0) {
                    surroundingStatements[0] = abstractStatements.get(i - 1);
                }
            }
            if(abstractStatement.equalFragment(lastCodeFragment)) {
                if(i < abstractStatements.size() - 1) {
                    surroundingStatements[1] = abstractStatements.get(i - 1);
                }
            }
        }
        return surroundingStatements;
    }

    /*
     * Gets the PSI elements for the extract method processor. If the surrounding statements are not null, use them to
     * get the PSI elements. If they are null, use the code fragments instead.
     */
    private ArrayList<PsiElement> getPsiElementsFromStatements(PsiStatement[] psiStatements,
                                                               AbstractCodeFragment firstCodeFragment,
                                                               AbstractCodeFragment lastCodeFragment,
                                                               AbstractStatement[] surroundingStatements) {
        AbstractStatement firstStatement = surroundingStatements[0];
        AbstractStatement lastStatement = surroundingStatements[1];

        ArrayList<PsiElement> psiElements = new ArrayList<>();
        boolean statementInRange = false;
        boolean startAfterFirstStatement = false;
        boolean stopBeforeLastStatement = false;
        String startingText = formatText(firstCodeFragment.getString());
        String endingText = formatText(lastCodeFragment.getString());
        if(firstStatement != null) {
            startingText = formatText(firstStatement.getString());
            startAfterFirstStatement = true;
        }
        if(lastStatement != null) {
            endingText = formatText(lastStatement.getString());
            stopBeforeLastStatement = true;
        }

        for(PsiStatement psiStatement : psiStatements) {
            String psiStatementText = formatText(psiStatement.getText());
            if(startingText.equals(psiStatementText)) {
                statementInRange = true;
                if(startAfterFirstStatement) {
                    continue;
                }
            }
            if(statementInRange) {
                ASTNode node = SourceTreeToPsiMap.psiElementToTree(psiStatement);
                assert node != null;
                ArrayList<ASTNode> nodes = getMethodNodes(node);
                for(ASTNode astNode : nodes) {
                    psiElements.add(astNode.getPsi());
                }
            }
            if(endingText.equals(psiStatementText)) {
                if(stopBeforeLastStatement) {
                    psiElements.remove(psiStatement);
                }
                break;
            }
        }
        return psiElements;

    }

    private ArrayList<ASTNode> getMethodNodes(ASTNode node) {
        ArrayList<ASTNode> astNodes = new ArrayList<>();

        PsiElement psiElement = node.getPsi();
        if((psiElement instanceof  PsiExpressionStatement) || (psiElement instanceof PsiDeclarationStatement)) {
            astNodes.add(node);
        }
        ASTNode[] children = node.getChildren(null);
        if(children.length < 1) {
            return astNodes;
        }

        for(ASTNode child : children) {
            astNodes.addAll(getMethodNodes(child));
        }

        return astNodes;
    }

    private PsiType getPsiReturnType(ExtractOperationRefactoring extractOperationRefactoring, PsiMethod psiMethod) {
        UMLParameter returnParameter = extractOperationRefactoring.getExtractedOperation().getReturnParameter();
        String parameterType = returnParameter.getType().toString();
        PsiElementFactory factory = PsiElementFactory.getInstance(project);
        return factory.createTypeFromText(parameterType, psiMethod);
    }
}
