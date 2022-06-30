package de.unipotsdam.se.papds.graphlib;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;

public class StaticFieldNode extends Node {

    private IClass klass;
    private IField field;

    public StaticFieldNode(IClass klass, IField field) {
        super(klass.toString() + "" + field.toString());
        this.klass = klass;
        this.field = field;
    }
}
