public class Main {
    public static void main(String[] args) {

    }
}

class NewA {
    int x;
    int y;

    public NewA(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void bar() {
        System.out.println(x + " " + y);
    }

    public int addition() {
        return x + y;
    }

    public int sub() {
        return x - y;
    }

    public int reverseSub() {
        return y - x;
    }
}

class NewB {
    double x;
    double y;

    public void doStuff() {
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