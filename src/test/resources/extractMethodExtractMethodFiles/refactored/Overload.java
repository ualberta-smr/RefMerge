

class Overload {

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

    public void source() {
        int x = getNum(5);
        int y = getNum();
        System.out.println("Adding " + x + " and " + y);
        int z = x + y;
        String str = "Printing: ";
        System.out.println(str + z);
    }

    private int getNum(int x) {
        return x;
    }

    private int getNum() {
        return 10;
    }

}