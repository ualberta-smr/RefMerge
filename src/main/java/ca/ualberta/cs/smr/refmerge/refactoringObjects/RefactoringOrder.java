package ca.ualberta.cs.smr.refmerge.refactoringObjects;

public enum RefactoringOrder {
    RENAME_PACKAGE(1),
    MOVE_RENAME_CLASS(2),
    INLINE_METHOD(3),
    MOVE_RENAME_METHOD(4),
    PULL_UP_METHOD(5),
    PUSH_DOWN_METHOD(6),
    RENAME_PARAMETER(7),
    EXTRACT_METHOD(8),
    MOVE_RENAME_FIELD(9),
    PULL_UP_FIELD(10),
    PUSH_DOWN_FIELD(11);


    private final int order;

    RefactoringOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

}
