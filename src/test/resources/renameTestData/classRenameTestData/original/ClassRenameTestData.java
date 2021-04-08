package renameTestData.classRenameTestData;

public class ClassRenameTestData {
    public void foo() {
        System.out.println("Do foo stuff");
    }

    protected static class Foo {
        public int numbers(int x, int y) {
            return x * y;
        }

        public void print(int x, int y) {
            System.out.println(x + ":" + y);
        }
    }
}