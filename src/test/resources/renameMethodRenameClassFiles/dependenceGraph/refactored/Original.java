class Original {
    int x;
    int y;

    public Original(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void fuzz() {
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