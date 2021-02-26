
public class Main {
    public static void doStuff() {
        int numStudents = getStudents();
        int classroom = getClassroomSize();
        boolean canFit = false;
        if(classroom - numStudents >= 0) {
            canFit = true;
        }
        System.out.println(canFit);
        HelperFile f = new HelperFile();
        f.set(classroom - numStudents);
    }

    public static int getStudents() {
        return 32;
    }

    public static int getClassroomSize() {
        return 112;
    }
}

class Child extends Main {
    public void getStudentInformation() {
        System.out.println("Do stuff");
    }
}

class ChildsChild extends Child {
    public void childSays() {
        System.out.println("I am the child's child. I inherit Main");
    }
}