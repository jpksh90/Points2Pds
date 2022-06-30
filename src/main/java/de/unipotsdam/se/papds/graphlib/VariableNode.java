/*
 * Copyright (c) 2018.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.graphlib;

import com.ibm.wala.classLoader.IMethod;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

public class VariableNode extends Node {

    /**
     * SSA transformation creates a unique variable number for each variable definition therefore, we it can be uniquely identified by vn for each method
     */
    private int vn; //Variable number
    private IMethod m; //Method number

    //Very bad programming practice. This should be a private method and only allowed to call from FunctionBasedHeapModel
    public VariableNode(int vn, IMethod m) {
        super(m.toString() + new Integer(vn).toString());
        this.vn = vn;
        this.m = m;
    }

    public VariableNode(VariableKey vk) {
        super(vk.getM().toString() + new Integer(vk.getVn()).toString());
        this.vn = vk.getVn();
        this.m = vk.getM();
    }

    public int getVn() {
        return vn;
    }

    public IMethod getMethod() {
        return m;
    }


    @Override
    public String toString() {
        return "VariableNode{" +
                "vn=" + vn +
                ", m=" + m +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VariableNode that = (VariableNode) o;

        if (vn != that.vn) return false;
        return m.equals(that.m);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + vn;
        result = 31 * result + m.hashCode();
        return result;
    }
}
