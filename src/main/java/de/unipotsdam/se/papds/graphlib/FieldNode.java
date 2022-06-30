package de.unipotsdam.se.papds.graphlib;

import com.ibm.wala.classLoader.IField;

public class FieldNode extends Node {

    private int vn;
    private IField f;

    public FieldNode(int vn, IField f) {
        super(new Integer(vn) + "." + f.toString());
        this.vn = vn;
        this.f = f;
    }
}
