

public class Override {

    public int foo() {
        int x = 0;
        for(int i = 0; i < 5; i++) {
            x += i;
        }
        return x;
    }

    public void method() {
        int x = getX();
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

    private int getX() {
        return 5;
    }

}

class SubClass extends Override {

    public void foo() {
        int x = getX();
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

    private int getX() {
        return 5;
    }

}