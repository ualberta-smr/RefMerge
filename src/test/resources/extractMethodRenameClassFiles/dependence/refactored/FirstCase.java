
public class Main {

    public boolean foo() {
        method();
        boolean bool = false;
        for(int i = 0; i < 10; i++) {
            if(bool) {
                return true;
            }
        }
        return bool;
    }

    public void method() {
        int x = getX();
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

    private void getX() {
        return 5;
    }

}


class Foo {

    public void fooBar() {
        foo();
    }

    private void foo() {
        int x = 5;
        int y = x * x;
        int z = y * x;
    }
}