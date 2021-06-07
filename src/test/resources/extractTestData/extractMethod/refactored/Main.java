

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
        extractedMethod(x, y);
    }

    private int getX() {
        return 5;
    }

    private void extractedMethod(int x, int y) {
        System.out.println("Adding " + x + " and " + y);
        System.out.println("Added Line");
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}
