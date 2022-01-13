package ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects;

/*
 * Contains the type and the name of a parameter
 */
public class ParameterObject {
    private String type;
    private String name;

    public ParameterObject(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public boolean equalsParameter(ParameterObject otherParameter) {
        return type.equals(otherParameter.getType()) && name.equals(otherParameter.getName());
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
}
