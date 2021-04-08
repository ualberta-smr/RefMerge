

public class Main {

    public int foobar() {
        int x = 0;
        for(int i = 0; i < 5; i++) {
            x += i;
        }
        return x;
    }

    public void newMethod() {
        int x = 5;
        int y = 10;
        numbers(x, y);
    }

    private void numbers(int x, int y) {
        System.out.println("Added Line");
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}

class SubClass extends Main {

    public boolean numbers() {
        boolean bool = false;
        for(int i = 0; i < 10; i++) {
            if(bool) {
                return true;
            }
        }
        return bool;
    }

}