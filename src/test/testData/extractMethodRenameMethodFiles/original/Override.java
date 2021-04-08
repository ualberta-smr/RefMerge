

public class Main {

    public int foo() {
        int x = 0;
        for(int i = 0; i < 5; i++) {
            x += i;
        }
        return x;
    }

    public void method() {
        int x = 5;
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}

class SubClass extends Main {

    public void foo() {
        for(int i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }

}