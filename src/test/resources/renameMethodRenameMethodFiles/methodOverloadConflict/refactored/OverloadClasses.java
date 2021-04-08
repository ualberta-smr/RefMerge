
class Foo {
    public void addNumbers(int x, int y) {
        int z = x + y;
    }
    public void numbers() {
        int x = 5;
        int y = x + x;
        int z = y + x;
    }

    public void subNumbers() {
        int x = 5;
        int y = x - 3;
        int z = y - x;
    }

    public void numbers(int x, int y) {
        int z = y * x;
    }
}

class Bar {
    public void doNumbers() {
        int x = 4;
        int y = x + x;
        int z = y * x;
    }
}