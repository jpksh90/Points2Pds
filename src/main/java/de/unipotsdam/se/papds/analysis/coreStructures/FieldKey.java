package de.unipotsdam.se.papds.analysis.coreStructures;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;

import java.util.Objects;

public class FieldKey {

    private IField field;
    private IClass klass;

    public FieldKey(IField field, IClass klass) {
        this.field = field;
        this.klass = klass;
    }

    public IField getField() {
        return field;
    }

    public IClass getKlass() {
        return klass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldKey fieldKey = (FieldKey) o;
        return Objects.equals(field, fieldKey.field) &&
                Objects.equals(klass, fieldKey.klass);
    }

    @Override
    public int hashCode() {

        return Objects.hash(field, klass);
    }

    @Override
    public String toString() {
        return "Field(" + field + "," + klass + ')';
    }
}
