class A {
    int x;
    int y;

    public A(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void bar() {
        System.out.println(x + " " + y);
    }

    public int add() {
        return x + y;
    }

    public int sub() {
        return x - y;
    }

    public int reverseSub() {
        return y - x;
    }
}