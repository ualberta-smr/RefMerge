package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.diff.ReorderParameterRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class ReorderParameterObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private final String refactoringDetail;


    private final MethodSignatureObject originalMethod;
    private final MethodSignatureObject destinationMethod;
    private final String originalClass;
    private final String destinationClass;
    private List<ParameterObject> originalParameterList;
    private List<ParameterObject> reorderedParameterList;


    public ReorderParameterObject(String originalClass, String destinationClass, MethodSignatureObject originalMethod,
                                  MethodSignatureObject destinationMethod) {
        this.refactoringType = RefactoringType.REORDER_PARAMETER;
        this.refactoringDetail = "";

        this.originalParameterList = new ArrayList<>();
        this.reorderedParameterList = new ArrayList<>();

        this.originalParameterList = originalMethod.getParameterList();
        this.reorderedParameterList = destinationMethod.getParameterList();

        this.originalMethod = originalMethod;
        this.destinationMethod = destinationMethod;
        this.originalClass = originalClass;
        this.destinationClass = destinationClass;

    }

    public ReorderParameterObject(Refactoring refactoring) {
        this.refactoringType = refactoring.getRefactoringType();
        this.refactoringDetail = refactoring.toString();

        ReorderParameterRefactoring reorderParameterRefactoring = (ReorderParameterRefactoring) refactoring;
        UMLOperation originalOperation = reorderParameterRefactoring.getOperationBefore();
        UMLOperation destinationOperation =  reorderParameterRefactoring.getOperationAfter();
        List<VariableDeclaration> originalParameters = reorderParameterRefactoring.getParametersAfter();
        List<VariableDeclaration> reorderedParameters = reorderParameterRefactoring.getParametersBefore();

        this.originalParameterList = new ArrayList<>();
        this.reorderedParameterList = new ArrayList<>();

        for(VariableDeclaration parameter : originalParameters) {
            String parameterType = parameter.getType().toString();
            String parameterName = parameter.getVariableName();
            this.originalParameterList.add(new ParameterObject(parameterType, parameterName));
        }
        for(VariableDeclaration parameter : reorderedParameters) {
            String parameterType = parameter.getType().toString();
            String parameterName = parameter.getVariableName();
            this.reorderedParameterList.add(new ParameterObject(parameterType, parameterName));
        }

        this.originalMethod = new MethodSignatureObject(originalOperation);
        this.destinationMethod = new MethodSignatureObject(destinationOperation);
        this.originalClass = originalOperation.getClassName();
        this.destinationClass = destinationOperation.getClassName();

    }

    @Override
    public void setStartLine(int startLine) {

    }

    @Override
    public void setEndLine(int endLine) {

    }

    @Override
    public int getStartLine() {
        return 0;
    }

    @Override
    public int getEndLine() {
        return 0;
    }

    @Override
    public String getRefactoringDetail() {
        return refactoringDetail;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return refactoringType;
    }

    @Override
    public RefactoringOrder getRefactoringOrder() {
        return null;
    }

    public String getOriginalClass() {
        return originalClass;
    }

    public String getDestinationClass() {
        return destinationClass;
    }

    public MethodSignatureObject getOriginalMethod() {
        return originalMethod;
    }

    public  MethodSignatureObject getDestinationMethod() {
        return destinationMethod;
    }

    public List<ParameterObject> getOriginalParameterList() {
        return originalParameterList;
    }

    public List<ParameterObject> getReorderedParameterList() {
        return reorderedParameterList;
    }

    @Override
    public void setOriginalFilePath(String originalFilePath) {

    }

    @Override
    public String getOriginalFilePath() {
        return null;
    }

    @Override
    public void setDestinationFilePath(String destinationFilePath) {

    }

    @Override
    public String getDestinationFilePath() {
        return null;
    }

    @Override
    public void setReplayFlag(boolean isReplay) {

    }

    @Override
    public boolean isReplay() {
        return false;
    }
}
