public class Main {
    public static void main(String[] args) {

    }
}

class A {
    int x;
    int y;

    public A(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void foo() {
        System.out.println(x + " " + y);
    }

    public int add() {
        return x + y;
    }

    public int sub() {
        return x - y;
    }

    public int reverseSub() {
        return y - x;
    }
}

class B {
    double x;
    double y;

    public void doBStuff() {
        double z = x / y;
    }

    private void method() {
        System.out.println("Method");
    }


    private void addToArray(x, y) {
        double[] array = new array[2];
        array[0] = x;
        array[1] = y;
    }
}