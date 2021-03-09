

public class Main {
    public static void main(String[] args) {

    }
}

class newA {
    int x;
    int y;

    public newA(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void bar() {
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