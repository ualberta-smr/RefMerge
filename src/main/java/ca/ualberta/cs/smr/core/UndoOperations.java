package ca.ualberta.cs.smr.core;

import ca.ualberta.cs.smr.core.refactoringWrappers.ExtractOperationRefactoringWrapper;
import ca.ualberta.cs.smr.core.refactoringWrappers.RefactoringWrapperUtils;
import ca.ualberta.cs.smr.utils.Utils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;

import com.intellij.refactoring.inline.InlineMethodProcessor;
import com.intellij.usageView.UsageInfo;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import org.refactoringminer.api.Refactoring;


public class UndoOperations {

    Project project;

    public UndoOperations(Project project) {
        this.project = project;
    }

    /*
     * Undo the rename method refactoring that was performed in the commit
     */
    public void undoRenameMethod(Refactoring ref) {
        UMLOperation original = ((RenameOperationRefactoring) ref).getOriginalOperation();
        UMLOperation renamed = ((RenameOperationRefactoring) ref).getRenamedOperation();
        String srcName = original.getName();
        String qualifiedClass = renamed.getClassName();
        // get the PSI class using the qualified class name
        String filePath = renamed.getLocationInfo().getFilePath();
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(qualifiedClass, filePath);
        assert psiClass != null;
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        PsiMethod method = Utils.getPsiMethod(psiClass, renamed);
        assert method != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(method, srcName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);
        // Update the virtual file that contains the refactoring
        vFile.refresh(false, true);

    }

    /*
     * Undo the class refactoring that was originally performed.
     */
    public void undoRenameClass(Refactoring ref) {
        UMLClass original = ((RenameClassRefactoring) ref).getOriginalClass();
        UMLClass renamed = ((RenameClassRefactoring) ref).getRenamedClass();
        String srcQualifiedClass = original.getName();
        String destQualifiedClass = renamed.getName();
        String srcClassName = srcQualifiedClass.substring(srcQualifiedClass.lastIndexOf(".") + 1);
        String filePath = renamed.getLocationInfo().getFilePath();
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(destQualifiedClass, filePath);
        assert psiClass != null;
        RefactoringFactory factory = JavaRefactoringFactory.getInstance(project);
        RenameRefactoring renameRefactoring = factory.createRename(psiClass, srcClassName, true, true);
        UsageInfo[] refactoringUsages = renameRefactoring.findUsages();
        renameRefactoring.doRefactoring(refactoringUsages);

        // Update the virtual file of the class
        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

    }

    /*
     * Undo the extract method refactoring that was originally performed by performing an inline method refactoring.
     */
    public ExtractOperationRefactoringWrapper undoExtractMethod(Refactoring ref) {
        ExtractOperationRefactoring extractOperationRefactoring = (ExtractOperationRefactoring) ref;
        UMLOperation sourceOperation = extractOperationRefactoring.getSourceOperationBeforeExtraction();
        UMLOperation extractedOperation = extractOperationRefactoring.getExtractedOperation();

        // Get PSI Method using extractedOperation data
        String extractedOperationClassName = extractedOperation.getClassName();
        String filePath = extractedOperation.getLocationInfo().getFilePath();
        Utils utils = new Utils(project);
        PsiClass psiClass = utils.getPsiClassFromClassAndFileNames(extractedOperationClassName, filePath);
        assert psiClass != null;
        PsiMethod extractedMethod = Utils.getPsiMethod(psiClass, extractedOperation);
        assert extractedMethod != null;

        PsiStatement[] surroundingStatements = getSurroundingStatements(extractedMethod);
        ExtractOperationRefactoringWrapper refactoringWrapper =
                    RefactoringWrapperUtils.wrapExtractOperation(extractOperationRefactoring, surroundingStatements);

        String sourceOperationClassName = sourceOperation.getClassName();
        filePath = sourceOperation.getLocationInfo().getFilePath();
        psiClass = utils.getPsiClassFromClassAndFileNames(sourceOperationClassName, filePath);
        PsiMethod psiMethod = Utils.getPsiMethod(psiClass, sourceOperation);
        assert psiMethod != null;

        PsiJavaCodeReferenceElement referenceElement = Utils.getPsiReferenceForExtractMethod(extractedOperation, psiMethod);
        // Set editor to null because we do not use the editor
        InlineMethodProcessor inlineMethodProcessor = new InlineMethodProcessor(project, extractedMethod, referenceElement,
                null, false);
        Application app = ApplicationManager.getApplication();
        app.invokeAndWait(inlineMethodProcessor);

        VirtualFile vFile = psiClass.getContainingFile().getVirtualFile();
        vFile.refresh(false, true);

        return refactoringWrapper;
    }


    private PsiStatement[] getSurroundingStatements(PsiMethod psiMethod) {
        PsiStatement[] surroundingStatements = new PsiStatement[2];
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        assert psiCodeBlock != null;
        PsiStatement[] psiStatements = psiCodeBlock.getStatements();
        int lastIndex = psiStatements.length - 1;
        if(lastIndex == 0) {
            return null;
        }
        surroundingStatements[0] = psiStatements[0];

        PsiStatement lastStatement = psiStatements[lastIndex];
        if(lastStatement instanceof PsiReturnStatement) {
            surroundingStatements[1] = psiStatements[lastIndex - 1];
        }
        else {
            surroundingStatements[1] = psiStatements[lastIndex];
        }
        return surroundingStatements;
    }

}
