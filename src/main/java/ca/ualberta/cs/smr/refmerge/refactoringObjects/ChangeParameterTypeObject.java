package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.diff.ChangeVariableTypeRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class ChangeParameterTypeObject implements RefactoringObject {


    private final RefactoringType refactoringType;
    private final String refactoringDetail;



    private ParameterObject originalParameter;
    private ParameterObject destinationParameter;
    private MethodSignatureObject originalMethod;
    private MethodSignatureObject destinationMethod;
    private String originalClass;
    private String destinationClass;


    public ChangeParameterTypeObject(String originalClass, String destinationClass,
                                     MethodSignatureObject originalMethod, MethodSignatureObject destinationMethod,
                                     ParameterObject originalParameter, ParameterObject destinationParameter) {
        this.refactoringType = RefactoringType.CHANGE_PARAMETER_TYPE;
        this.refactoringDetail = "";

        this.originalClass = originalClass;
        this.destinationClass = destinationClass;
        this.originalMethod = originalMethod;
        this.destinationMethod = destinationMethod;
        this.originalParameter = originalParameter;
        this.destinationParameter = destinationParameter;

    }


    public ChangeParameterTypeObject(Refactoring refactoring) {
        ChangeVariableTypeRefactoring ref = (ChangeVariableTypeRefactoring) refactoring;
        this.refactoringType = ref.getRefactoringType();
        this.refactoringDetail = ref.toString();


        VariableDeclaration originalParameter = ref.getChangedTypeVariable();
        String parameterName = originalParameter.getVariableName();
        String parameterType = originalParameter.getType().toString();
        this.originalParameter = new ParameterObject(parameterType, parameterName);

        VariableDeclaration destinationParameter = ref.getChangedTypeVariable();
        parameterName = destinationParameter.getVariableName();
        parameterType = destinationParameter.getType().toString();
        this.destinationParameter = new ParameterObject(parameterType, parameterName);

        UMLOperation originalOperation = ref.getOperationBefore();
        UMLOperation destinationOperation = ref.getOperationAfter();
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

    public ParameterObject getOriginalParameter() {
        return originalParameter;
    }

    public void setOriginalParameter(ParameterObject originalParameter) {
        this.originalParameter = originalParameter;
    }

    public ParameterObject getDestinationParameter() {
        return destinationParameter;
    }

    public void setDestinationParameter(ParameterObject destinationParameter) {
        this.destinationParameter = destinationParameter;
    }

    public MethodSignatureObject getOriginalMethod() {
        return originalMethod;
    }

    public void setOriginalMethod(MethodSignatureObject originalMethod) {
        this.originalMethod = originalMethod;
    }

    public MethodSignatureObject getDestinationMethod() {
        return destinationMethod;
    }

    public void setDestinationMethod(MethodSignatureObject destinationMethod) {
        this.destinationMethod = destinationMethod;
    }

    public String getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(String originalClass) {
        this.originalClass = originalClass;
    }

    public String getDestinationClass() {
        return destinationClass;
    }

    public void setDestinationClass(String destinationClass) {
        this.destinationClass = destinationClass;
    }

    @Override
    public RefactoringOrder getRefactoringOrder() {
        return null;
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
