
public class Main {
    public static void checkIfClassroomWorks() {
        int numStudents = getStudents();
        int classroom = getClassroomSize();
        boolean canFit = false;
        if(classroom - numStudents >= 0) {
            canFit = true;
        }
        System.out.println(canFit);
        ClassFile cf = new ClassFile();
        cf.set(classroom - numStudents);
    }

    public static int getStudents() {
        return 32;
    }

    public static int getClassroomSize() {
        return 112;
    }
}

class ChildClass extends Main {
    public void getStudentInformation() {
        System.out.println("Do stuff");
    }
}

class ChildsChild extends ChildClass {
    public void childSays() {
        System.out.println("I am the child's child. I inherit Main");
    }
}