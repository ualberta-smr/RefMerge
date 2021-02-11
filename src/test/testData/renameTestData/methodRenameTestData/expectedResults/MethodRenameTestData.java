package result.renameTestData.methodRenameTestData;

public class MethodRenameTestData {

    public void foo() {
        int val = 5;
        int y = val * val;
        graph(val, y);
    }

    public void foo(int x) {
        int y = x + x;
        System.out.println(x + " " + y);
        doStuff();
    }

    public void graph(int x, int y) {
        System.out.println(x + " " + y);
    }

    public void doStuff() {
        System.out.println("Doing stuff");
    }

}