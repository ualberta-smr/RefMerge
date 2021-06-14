package ca.ualberta.cs.smr.core.refactoringObjects;

public class ClassObject {
    private String className;
    private String packageName;

    public ClassObject(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean equalsClass(ClassObject otherClass) {
        if(!this.packageName.equals(otherClass.packageName)) {
            return false;
        }
        return this.className.equals(otherClass.className);
    }
}
