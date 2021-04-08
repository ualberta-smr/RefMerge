

public class Main {

    public boolean bar() {
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
        int x = 5;
        int y = 10;
        bar(x, y);
    }
    private void bar(int x, int y) {
        System.out.println("Added Line");
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}