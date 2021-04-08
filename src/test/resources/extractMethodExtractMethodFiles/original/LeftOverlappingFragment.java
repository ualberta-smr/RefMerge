

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
        int x = 5;
        int y = 10;
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

}