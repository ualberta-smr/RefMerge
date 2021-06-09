package ca.ualberta.cs.smr.core.refactoringObjects;

public enum RefactoringOrder {
    RENAME_CLASS(1),
    RENAME_METHOD(2),
    EXTRACT_METHOD(3);

    private final int order;

    RefactoringOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

}
