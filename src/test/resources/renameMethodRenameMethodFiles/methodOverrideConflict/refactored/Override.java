public class ParentClass {
    String foo;

    public FooClass() {
        foo = "foo";
    }

    public void numbers() {
        int x = 5;
        int y = x + x;
        int z = y + x;
    }


    public void multNumbers() {
        int x = 5;
        int y = x * x;
        int z = y * x;
    }

    public void doStuffWithValues() {
        System.out.println("Doing stuff with values");
    }
}

public class ChildClass extends ParentClass {
    String foo;

    public BarClass(String fooS) {
        super();
    }


    public void numbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }

    public void subNumbers() {
        int x = 5;
        int y = x - x;
        int z = y - x;
    }

    public void doStuffWithValues() {
        System.out.println("Doing stuff with values");
    }
}

class FooBar {
    public void otherNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }
}