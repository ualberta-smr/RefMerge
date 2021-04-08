

class OverlappingRegion {

    public int foo() {
        method();
        boolean bool = false;
        for(int i = 0; i < 10; i++) {
            if(i > 5) {
                bool = true;
                return 0;
            }
        }
        return 1;
    }

    public void method() {
        int x = getX();
        int y = 10;
        for(int i = 0; i < 5; i++) {
            System.out.println(x);
        }
        for(int i = 0; i < 5; i++) {
            System.out.println(y);
        }
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        print(z);
    }

    private int getX() {
        return 5;
    }

    private void print(int z) {
        String str = "Printing: ";
        System.out.println(str + z);
    }

}