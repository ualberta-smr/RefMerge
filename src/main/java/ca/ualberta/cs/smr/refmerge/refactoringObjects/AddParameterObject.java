package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.ParameterObject;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;
import gr.uom.java.xmi.diff.AddParameterRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

public class AddParameterObject implements RefactoringObject {

    private final String refactoringDetail;
    private final RefactoringType refactoringType;
    private ParameterObject parameterObject;
    private MethodSignatureObject originalMethod;
    private MethodSignatureObject destinationMethod;
    private String originalClass;
    private String destinationClass;

    public AddParameterObject(String originalClass, String destinationClass, MethodSignatureObject originalMethod,
                              MethodSignatureObject destinationMethod, ParameterObject parameterObject) {
        this.refactoringDetail = "";
        this.refactoringType = RefactoringType.ADD_PARAMETER;
        this.parameterObject = parameterObject;
        this.originalMethod = originalMethod;
        this.destinationMethod = destinationMethod;
        this.originalClass = originalClass;
        this.destinationClass = destinationClass;
    }

    public AddParameterObject(Refactoring refactoring) {
        AddParameterRefactoring ref = (AddParameterRefactoring) refactoring;
        this.refactoringDetail = ref.toString();
        this.refactoringType = ref.getRefactoringType();
        UMLParameter parameter = ref.getParameter();
        this.parameterObject = new ParameterObject(parameter.getType().toString(), parameter.getName());
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

    @Override
    public RefactoringOrder getRefactoringOrder() {
        return null;
    }

    public void setParameterObject(ParameterObject parameterObject) {
        this.parameterObject = parameterObject;
    }

    public ParameterObject getParameterObject() {
        return parameterObject;
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

    public void setOriginalClass(String originalClass) {
        this.originalClass = originalClass;
    }

    public String getOriginalClass() {
        return originalClass;
    }

    public void setDestinationClass(String destinationClass) {
        this.destinationClass = destinationClass;
    }

    public String getDestinationClass() {
        return destinationClass;
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
