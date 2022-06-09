package ca.ualberta.cs.smr.refmerge.refactoringObjects;

import com.intellij.openapi.util.Pair;
import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.diff.PushDownAttributeRefactoring;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import java.util.ArrayList;
import java.util.List;

public class PushDownFieldObject implements RefactoringObject {

    private final RefactoringType refactoringType;
    private String originalClass;
    private String newClass;
    private String originalFileName;
    private String refactoredFileName;
    private String originalFieldName;
    private String refactoredFieldName;
    private List<Pair<String, String>> subClasses;
    private final String refactoringDetail;
    private boolean isReplay;

    public PushDownFieldObject(String originalClass, String originalMethodName, String newClass, String refactoredMethodName) {
        this.refactoringType = RefactoringType.PUSH_DOWN_OPERATION;
        this.originalClass = originalClass;
        this.newClass = newClass;
        this.originalFieldName = originalMethodName;
        this.refactoredFieldName = refactoredMethodName;
        this.originalFileName = originalClass;
        this.refactoredFileName = newClass;

        this.subClasses = new ArrayList<>();
        this.subClasses.add(new Pair<>(newClass, refactoredFileName));
        this.isReplay = true;
        this.refactoringDetail = "";

    }

    public PushDownFieldObject(Refactoring refactoring) {
        PushDownAttributeRefactoring pushDownAttributeRefactoring = (PushDownAttributeRefactoring) refactoring;
        this.refactoringType = refactoring.getRefactoringType();
        UMLAttribute originalAttribute = pushDownAttributeRefactoring.getOriginalAttribute();
        UMLAttribute refactoredAttribute = pushDownAttributeRefactoring.getMovedAttribute();
        this.originalClass = originalAttribute.getClassName();
        this.newClass = refactoredAttribute.getClassName();
        this.originalFieldName = originalAttribute.getName();
        this.refactoredFieldName = refactoredAttribute.getName();
        this.originalFileName = originalAttribute.getLocationInfo().getFilePath();
        this.refactoredFileName = refactoredAttribute.getLocationInfo().getFilePath();
        this.refactoringDetail = refactoring.toString();
        this.subClasses = new ArrayList<>();
        this.subClasses.add(new Pair<>(newClass, refactoredFileName));
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

    public String getOriginalFieldName() {
        return originalFieldName;
    }

    public void setOriginalFieldName(String originalFieldName) {
        this.originalFieldName = originalFieldName;
    }

    public String getRefactoredFieldName() {
        return refactoredFieldName;
    }

    public void setRefactoredFieldName(String refactoredFieldName) {
        this.refactoredFieldName = refactoredFieldName;
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
        return RefactoringOrder.PUSH_DOWN_FIELD;
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

    public String getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(String originalClass) {
        this.originalClass = originalClass;
    }

    public String getTargetSubClass() {
        return newClass;
    }

    public void setTargetSubClass(String newClass) {
        this.newClass = newClass;
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
