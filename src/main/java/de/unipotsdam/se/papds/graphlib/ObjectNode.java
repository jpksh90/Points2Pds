/*
 * Copyright (c) 2018.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.graphlib;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;

import java.util.Objects;

/*
    An  Object is identified by its type, method and its' linenumber
 */

public class ObjectNode extends Node {

    /*
        A Object is identified by its type, method containing allocation and lineno of the method
     */
    private TypeName type;
    private IMethod method;
    private int lineno;

    public ObjectNode() {
    }

    public ObjectNode(TypeName type, IMethod m, int lineno) {
        super(type.toString());
        this.method = m;
        this.type = type;
        this.lineno = lineno;
    }

    public TypeName getType() {
        return type;
    }

    public IMethod getMethod() {
        return method;
    }

    public int getLineno() {
        return lineno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ObjectNode that = (ObjectNode) o;
        return lineno == that.lineno &&
                Objects.equals(type, that.type) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), type, method, lineno);
    }

    @Override
    public String toString() {
        return "ObjectNode{" +
                "type=" + type +
                ", method=" + method +
                ", lineno=" + lineno +
                '}';
    }
}
