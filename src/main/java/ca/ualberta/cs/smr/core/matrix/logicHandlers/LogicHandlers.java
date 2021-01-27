package ca.ualberta.cs.smr.core.matrix.logicHandlers;

public class LogicHandlers {

    public static boolean isTheSameName(String elementName, String visitorName) {
        return elementName.equals(visitorName);
    }

    public static boolean ifClassExtends(Class elementClass, Class visitorClass) {
        if(elementClass.isAssignableFrom(visitorClass) || visitorClass.isAssignableFrom(elementClass)) {
            return true;
        }
        return false;
    }
}
