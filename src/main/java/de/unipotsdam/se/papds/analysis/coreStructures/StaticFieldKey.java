/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

//Models static Field
package de.unipotsdam.se.papds.analysis.coreStructures;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

public class StaticFieldKey implements PointerKey {

    IClass klass;
    IField field;

    public StaticFieldKey(IClass klass, IField field) {
        this.klass = klass;
        this.field = field;
    }

    public IClass getKlass() {
        return klass;
    }

    public IField getField() {
        return field;
    }
}
