package renameTestData.methodRenameTestData;

// Renamed foo(int x) to bar(int x)
public class MethodRenameTestData {

    public void foo() {
        int val = 5;
        int y = val * val;
        graph(val, y);
    }

    public void bar(int x) {
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