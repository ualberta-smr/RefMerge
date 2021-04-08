
public class ParentClass {
    String foo;

    public FooClass() {
        foo = "foo";
    }


    public void addNumbers() {
        int x = 5;
        int y = x + x;
        int z = y + x;
    }


    public void doNumbers() {
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

    public void doNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }

    public void subtractNumbers() {
        int x = 5;
        int y = x - x;
        int z = y - x;
    }

    public void doStuffWithValues() {
        System.out.println("Doing stuff with values");
    }
}

class FooBar {
    public void doNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }
}