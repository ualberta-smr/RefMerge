package ca.ualberta.cs.smr.core.matrix.logicHandlers;

public class LogicHandlers {

    static public boolean isSameName(String elementName, String visitorName) {
        return elementName.equals(visitorName);
    }

    static public boolean ifClassExtends(Class elementClass, Class visitorClass) {
        return elementClass.isAssignableFrom(visitorClass) || visitorClass.isAssignableFrom(elementClass);
    }

}
