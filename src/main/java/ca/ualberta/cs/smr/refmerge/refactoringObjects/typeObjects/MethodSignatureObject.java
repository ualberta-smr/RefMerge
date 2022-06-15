package ca.ualberta.cs.smr.refmerge.refactoringObjects.typeObjects;

import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLParameter;

import java.util.ArrayList;
import java.util.List;

/*
 * Contains the name of a method as well as the parameters in the method signature
 */
public class MethodSignatureObject {

    private String name;
    private List<ParameterObject> parameterList;
    private final String visibility;
    private final boolean isConstructor;
    private final boolean isStatic;

    /*
     * Create the method signature from the name and the given parameter object list
     */
    public MethodSignatureObject( List<ParameterObject> parameterList, String name) {
        this.name = name;
        this.parameterList = parameterList;
        this.isConstructor = false;
        this.visibility = "public";
        this.isStatic = false;
    }

    public MethodSignatureObject(UMLOperation method) {
        this.name = method.getName();
        this.parameterList = new ArrayList<>();
        List<UMLParameter> parameters = method.getParameters();
        for(UMLParameter parameter : parameters) {
            this.parameterList.add(new ParameterObject(parameter.getType().toString(), parameter.getName()));
        }
        this.visibility = method.getVisibility();
        this.isConstructor = method.isConstructor();
        this.isStatic = method.isStatic();
    }

    /*
     * Create the method signature from the name and UML parameter list
     */
    public MethodSignatureObject(String name, List<UMLParameter> umlParameterList, boolean isConstructor,
                                 String visibility, boolean isStatic) {
        this.name = name;
        this.parameterList = new ArrayList<>();
        for(UMLParameter umlParameter : umlParameterList) {
            this.parameterList.add(new ParameterObject(umlParameter.getType().toString(), umlParameter.getName()));
        }
        this.visibility = visibility;
        this.isConstructor = isConstructor;
        this.isStatic = isStatic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ParameterObject> getParameterList() {
        return parameterList;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isStatic() {
        return isStatic;
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

    public boolean equalsSignatureExcludingParameterNames(MethodSignatureObject otherSignature) {
        if(!this.name.equals(otherSignature.getName())) {
            return false;
        }
        if(this.parameterList.size() != otherSignature.getParameterList().size()) {
            return false;
        }
        List<ParameterObject> otherParameterList = otherSignature.getParameterList();
        for(int i = 0; i < this.parameterList.size(); i++) {
            String thisType = parameterList.get(i).getType();
            String otherType = otherParameterList.get(i).getType();
            if(!thisType.equals(otherType)) {
                return false;
            }
        }

        return true;
    }

    public int getParameterLocation(ParameterObject parameterObject) {
        return parameterList.indexOf(parameterObject);
    }

    public void updateParameterAtLocation(int location, ParameterObject parameterObject) {
        if(location > -1) {
            this.parameterList.set(location, parameterObject);
        }
        // If location == -1, add parameter to end
        else {
            this.parameterList.add(parameterObject);
        }
    }

    public void addParameterAtLocation(int location, ParameterObject parameterObject) {
        if(location > -1) {
            this.parameterList.add(location, parameterObject);
        }
        // If location == -1, add parameter to end
        else {
            this.parameterList.add(parameterObject);
        }
    }


    public void removeParameterAtLocation(int location) {
        this.parameterList.remove(location);
    }

    public ParameterObject getReturnParameter() {
        for(ParameterObject parameterObject : parameterList) {
            if(parameterObject.getName().equals("return")) {
                return parameterObject;
            }
        }
        return null;
    }

    public void replaceParameterList(List<ParameterObject> parameterList) {
        this.parameterList = parameterList;
    }
}
