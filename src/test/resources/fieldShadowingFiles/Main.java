public class Main {

    int aField;
    double doubleField;

    public static void main(String[] args) {

    }

}

class SubClass extends Main {

    int aField;

    public int graph(int x, int y) {
        System.out.println(x, y);
        return x * y;
    }

}