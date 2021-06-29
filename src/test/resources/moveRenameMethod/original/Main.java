

public class Main {

    public static void foo() {
        int x = 5;
        int y = 10;
        boolean isEqual = OtherClass.equals(x, y);
        String s = x + " " + y + " " + isEqual;
    }

    public String bar() {
        return "bar";
    }

    public int foobar(int x, double y) {
        return x;
    }
}