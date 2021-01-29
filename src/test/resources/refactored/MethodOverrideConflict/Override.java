
public class Foo {
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
}

class Bar extends Foo {
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
}

class FooBar {
    public void doNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }
}