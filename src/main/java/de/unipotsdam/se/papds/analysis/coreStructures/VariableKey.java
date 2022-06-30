/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.analysis.coreStructures;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

public class VariableKey implements PointerKey {

    private int vn;
    private IMethod m;

    public VariableKey(int vn, IMethod m) {
        this.vn = vn;
        this.m = m;
    }

    public int getVn() {
        return vn;
    }

    public IMethod getM() {
        return m;
    }
}
