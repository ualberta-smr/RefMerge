package ca.ualberta.cs.smr.core.refactoringObjects;

import gr.uom.java.xmi.UMLParameter;

import java.util.ArrayList;
import java.util.List;

/*
 * Contains the name of a method as well as the parameters in the method signature
 */
public class MethodSignatureObject {

    private String name;
    private List<ParameterObject> parameterList;
    private String visibility;
    private boolean isConstructor;

    /*
     * Create the method signature from the name and the given parameter object list
     */
    public MethodSignatureObject( List<ParameterObject> parameterList, String name) {
        this.name = name;
        this.parameterList = parameterList;
        this.isConstructor = false;
        this.visibility = "public";
    }

    /*
     * Create the method signature from the name and UML parameter list
     */
    public MethodSignatureObject(String name, List<UMLParameter> umlParameterList, boolean isConstructor, String visibility) {
        this.name = name;
        this.parameterList = new ArrayList<>();
        for(UMLParameter umlParameter : umlParameterList) {
            this.parameterList.add(new ParameterObject(umlParameter.getType().toString(), umlParameter.getName()));
        }
        this.visibility = visibility;
        this.isConstructor = isConstructor;
    }

    public String getName() {
        return this.name;
    }

    public List<ParameterObject> getParameterList() {
        return this.parameterList;
    }

    public boolean isConstructor() {
        return this.isConstructor;
    }

    public String getVisibility() {
        return this.visibility;
    }

    public boolean equalsSignature(MethodSignatureObject otherSignature) {
        if(!this.name.equals(otherSignature.getName())) {
            return false;
        }
        if(this.parameterList.size() != otherSignature.getParameterList().size()) {
            return false;
        }
        List<ParameterObject> otherParameterList = otherSignature.getParameterList();
        for(int i = 0; i < this.parameterList.size(); i++) {
            ParameterObject thisParameter = parameterList.get(i);
            ParameterObject otherParameter = otherParameterList.get(i);
            if(!thisParameter.equalsParameter(otherParameter)) {
                return false;
            }
        }

        return true;
    }

    public ParameterObject getReturnParameter() {
        for(ParameterObject parameterObject : parameterList) {
            if(parameterObject.getName().equals("return")) {
                return parameterObject;
            }
        }
        return null;
    }
}
