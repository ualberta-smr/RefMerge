package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects.MethodSignatureObject;
import com.intellij.openapi.util.Pair;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.diff.PushDownOperationRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class PushDownMethodObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private String originalClass;
    private String targetBaseClass;
    private String originalMethodName;
    private String refactoredMethodName;
    private String originalFileName;
    private String refactoredFileName;
    private MethodSignatureObject originalMethodSignature;
    private MethodSignatureObject destinationMethodSignature;
    private List<Pair<String, String>> subClasses;
    private final String refactoringDetail;
    private boolean isReplay;

    public PushDownMethodObject(String originalClass, String originalMethodName, String newClass, String refactoredMethodName) {
        this.refactoringType = RefactoringType.PUSH_DOWN_OPERATION;
        this.originalClass = originalClass;
        this.targetBaseClass = newClass;
        this.originalMethodName = originalMethodName;
        this.refactoredMethodName = refactoredMethodName;
        this.originalFileName = originalClass;
        this.refactoredFileName = newClass;

        this.originalMethodSignature = new MethodSignatureObject(originalMethodName, new ArrayList<>(),
                false, "", true);
        this.destinationMethodSignature = new MethodSignatureObject(refactoredMethodName, new ArrayList<>(),
                false, "", true);

        this.subClasses = new ArrayList<>();
        this.subClasses.add(new Pair<>(targetBaseClass, refactoredFileName));
        this.isReplay = true;
        this.refactoringDetail = "";

    }

    public PushDownMethodObject(Refactoring refactoring) {
        PushDownOperationRefactoring pushDownOperation = (PushDownOperationRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        UMLOperation originalOperation =  pushDownOperation.getOriginalOperation();
        UMLOperation refactoredOperation =  pushDownOperation.getMovedOperation();
        this.originalClass = originalOperation.getClassName();
        this.targetBaseClass = refactoredOperation.getClassName();
        this.originalMethodName = originalOperation.getName();
        this.refactoredMethodName = refactoredOperation.getName();
        this.originalFileName = originalOperation.getLocationInfo().getFilePath();
        this.refactoredFileName = refactoredOperation.getLocationInfo().getFilePath();
        this.originalMethodSignature = new MethodSignatureObject(originalOperation.getName(), originalOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.destinationMethodSignature = new MethodSignatureObject(refactoredOperation.getName(), refactoredOperation.getParameters(),
                originalOperation.isConstructor(), originalOperation.getVisibility(), originalOperation.isStatic());
        this.refactoringDetail = refactoring.toString();
        this.subClasses = new ArrayList<>();
        this.subClasses.add(new Pair<>(targetBaseClass, refactoredFileName));
        this.isReplay = true;
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
        return RefactoringOrder.PUSH_DOWN_METHOD;
    }

    @Override
    public void setOriginalFilePath(String originalFilePath) {
        this.originalFileName = originalFilePath;
    }

    @Override
    public String getOriginalFilePath() {
        return originalFileName;
    }

    @Override
    public void setDestinationFilePath(String destinationFilePath) {
        this.refactoredFileName = destinationFilePath;
    }

    @Override
    public String getDestinationFilePath() {
        return refactoredFileName;
    }

    public MethodSignatureObject getOriginalMethodSignature() {
        return originalMethodSignature;
    }

    public void setOriginalMethodSignature(MethodSignatureObject originalMethodSignature) {
        this.originalMethodSignature = originalMethodSignature;
    }

    public MethodSignatureObject getDestinationMethodSignature() {
        return destinationMethodSignature;
    }

    public void setDestinationMethodSignature(MethodSignatureObject destinationMethodSignature) {
        this.destinationMethodSignature = destinationMethodSignature;
    }

    public String getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(String originalClass) {
        this.originalClass = originalClass;
    }

    public String getTargetBaseClass() {
        return targetBaseClass;
    }

    public void setTargetBaseClass(String newClass) {
        this.targetBaseClass = newClass;
    }

    @Override
    public void setReplayFlag(boolean isReplay) {
        this.isReplay = isReplay;
    }

    @Override
    public boolean isReplay() {
        return isReplay;
    }

    public List<Pair<String, String>> getSubClasses() {
        return this.subClasses;
    }

    public void addSubClass(String className, String fileName) {
        subClasses.add(new Pair<>(className, fileName));
    }

    public void addSubClass(Pair<String, String> pair) {
        subClasses.add(pair);
    }
}
