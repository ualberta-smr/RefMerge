package ca.ualberta.cs.smr.refmerge.refactoringObjects;

public enum RefactoringOrder {
    RENAME_PACKAGE(1),
    MOVE_RENAME_CLASS(2),
    INLINE_METHOD(3),
    MOVE_RENAME_METHOD(4),
    PULL_UP_METHOD(5),
    PUSH_DOWN_METHOD(6),
    EXTRACT_METHOD(7),
    MOVE_RENAME_FIELD(8),
    PULL_UP_FIELD(9),
    PUSH_DOWN_FIELD(10);


    private final int order;

    RefactoringOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

}
