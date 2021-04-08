

public class Overload {

    public boolean check() {
        source();
        boolean bool = false;
        for(int i = 0; i < 10; i++) {
            if(bool) {
                return true;
            }
        }
        return bool;
    }

    public void source() {
        int x = 5;
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}