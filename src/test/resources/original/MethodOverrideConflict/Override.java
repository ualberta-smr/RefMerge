
public class Foo {
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
}

class Bar extends Foo {
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
}

class FooBar {
    public void otherNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }
}